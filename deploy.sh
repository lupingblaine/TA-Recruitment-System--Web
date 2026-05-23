#!/bin/bash
# TA&MO 一键部署脚本
# 用法: ./deploy.sh [full|static|data]
#   full   - 完整部署（编译+上传+重启）
#   static - 仅同步 JSP/静态资源（不重启）
#   data   - 仅将本地 data.json 同步到服务器并重启（覆盖服务器数据）

SERVER="root@47.93.90.208"
REMOTE_DIR="/root/TA_MO_latest/backend"
LOCAL_BACKEND="$(cd "$(dirname "$0")/backend" && pwd)"
SSH_CTRL="${TMPDIR:-/tmp}/tamo-deploy-$$.sock"

set -e

ssh_master_start() {
    ssh -o ControlMaster=yes -o ControlPath="$SSH_CTRL" -o ControlPersist=120 -fN "$SERVER"
}

ssh_master_stop() {
    ssh -O exit -o ControlPath="$SSH_CTRL" "$SERVER" 2>/dev/null || true
    rm -f "$SSH_CTRL"
}

ssh_run() {
    ssh -o ControlPath="$SSH_CTRL" -T "$SERVER" "$@"
}

scp_run() {
    scp -o ControlPath="$SSH_CTRL" "$@"
}

# 轮询 app.log，最多等待 30 秒（Tomcat 冷启动常超过 4 秒）
wait_for_startup() {
    local i
    for i in $(seq 1 30); do
        if ssh_run "grep -q '已启动' $REMOTE_DIR/app.log 2>/dev/null"; then
            return 0
        fi
        sleep 1
    done
    return 1
}

restart_remote_app() {
    ssh_run bash -s << 'REMOTE_SCRIPT'
cd /root/TA_MO_latest/backend
pkill -9 -f "com.tamo.Main" 2>/dev/null || true
sleep 1
> app.log
setsid java -cp "target/classes:target/dependency/*" com.tamo.Main > app.log 2>&1 &
disown
REMOTE_SCRIPT
    if wait_for_startup; then
        echo "=== 服务启动成功 ==="
        ssh_run "tail -8 $REMOTE_DIR/app.log"
        return 0
    fi
    echo "=== 启动超时或失败，最近日志 ==="
    ssh_run "tail -40 $REMOTE_DIR/app.log" || true
    return 1
}

deploy_full() {
    echo "=== 1/5 编译项目 ==="
    cd "$LOCAL_BACKEND"
    mvn clean package dependency:copy-dependencies -DoutputDirectory=target/dependency -DskipTests

    echo "=== 2/5 打包文件（含 webapp 资源）==="
    tar --no-xattrs --no-mac-metadata -czf backend-dist.tar.gz -C target classes dependency
    tar --no-xattrs --no-mac-metadata -czf webapp-dist.tar.gz src/main/webapp

    echo "=== 3/5 上传到服务器 ==="
    scp backend-dist.tar.gz webapp-dist.tar.gz "$SERVER:$REMOTE_DIR/"

    echo "=== 4/5 服务器部署并重启 ==="
    ssh -T "$SERVER" << 'REMOTE_SCRIPT'
cd /root/TA_MO_latest/backend

# 停止旧进程
pkill -9 -f "com.tamo.Main" 2>/dev/null || true
sleep 1

# 保留服务器上正在使用的 data.json（避免全量部署覆盖生产数据）
DATA_BACKUP=""
if [ -f target/classes/data.json ]; then
  DATA_BACKUP=$(mktemp)
  cp target/classes/data.json "$DATA_BACKUP"
fi

# 解压新的编译文件
rm -rf target/classes target/dependency
mkdir -p target
tar -xzf backend-dist.tar.gz -C target

if [ -n "$DATA_BACKUP" ] && [ -f "$DATA_BACKUP" ]; then
  cp "$DATA_BACKUP" target/classes/data.json
  rm -f "$DATA_BACKUP"
  echo "已保留服务器 data.json"
fi

# 解压新的 webapp 资源
tar -xzf webapp-dist.tar.gz

# 清除 Tomcat JSP 编译缓存
rm -rf tomcat.8080

# 启动服务（使用 setsid 确保 SSH 断开后进程不被杀）
> app.log
setsid java -cp "target/classes:target/dependency/*" com.tamo.Main > app.log 2>&1 &
disown

for i in $(seq 1 30); do
  if grep -q "已启动" app.log 2>/dev/null; then
    echo "=== 服务启动成功 ==="
    tail -8 app.log
    exit 0
  fi
  sleep 1
done
echo "=== 启动超时，最近日志 ==="
tail -40 app.log
exit 1
REMOTE_SCRIPT

    echo ""
    echo "=== 5/5 部署完成! ==="
    echo "访问: http://47.93.90.208:8080/tamo"
    rm -f backend-dist.tar.gz webapp-dist.tar.gz
}

deploy_static() {
    echo "=== 同步静态资源 ==="
    tar --no-xattrs --no-mac-metadata -czf webapp-dist.tar.gz -C "$LOCAL_BACKEND" src/main/webapp
    scp webapp-dist.tar.gz "$SERVER:$REMOTE_DIR/"
    ssh -T "$SERVER" "cd $REMOTE_DIR && tar -xzf webapp-dist.tar.gz && rm -rf tomcat.8080"
    rm -f webapp-dist.tar.gz
    echo "=== 同步完成（JSP 缓存已清除，无需重启）==="
}

deploy_data() {
    LOCAL_DATA="$LOCAL_BACKEND/src/main/resources/data.json"
    if [ ! -f "$LOCAL_DATA" ]; then
        echo "错误: 找不到 $LOCAL_DATA"
        exit 1
    fi

    echo "=== 同步 data.json（将覆盖服务器上的数据）==="
    echo "本地文件: $LOCAL_DATA ($(wc -c < "$LOCAL_DATA" | tr -d ' ') bytes)"
    echo "（仅需输入一次 SSH 密码）"

    trap ssh_master_stop EXIT
    ssh_master_start

    ssh_run bash -s << REMOTE_BACKUP
cd $REMOTE_DIR
mkdir -p target/classes src/main/resources
if [ -f target/classes/data.json ]; then
  cp target/classes/data.json "target/classes/data.json.bak.\$(date +%Y%m%d_%H%M%S)"
  echo "已备份服务器旧 data.json 到 target/classes/"
fi
REMOTE_BACKUP

    scp_run "$LOCAL_DATA" "$SERVER:$REMOTE_DIR/target/classes/data.json"
    scp_run "$LOCAL_DATA" "$SERVER:$REMOTE_DIR/src/main/resources/data.json"
    echo "data.json 已上传到服务器"

    echo "=== 重启服务以加载新数据 ==="
    restart_remote_app || { echo "提示: 数据文件已更新，若站点可访问则无需重跑"; exit 1; }
    ssh_run "ls -la $REMOTE_DIR/target/classes/data.json"

    ssh_master_stop
    trap - EXIT

    echo ""
    echo "=== data.json 同步完成 ==="
    echo "访问: http://47.93.90.208:8080/tamo"
}

case "${1:-full}" in
    full)   deploy_full ;;
    static) deploy_static ;;
    data)   deploy_data ;;
    *)      echo "用法: ./deploy.sh [full|static|data]"; exit 1 ;;
esac
