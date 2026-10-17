# 本地 PostgreSQL 使用说明

数据库在自己的电脑上运行，无需云服务账号、信用卡或数据库订阅。

## 启动与停止

先启动 Docker Desktop。在项目根目录运行：

```bash
./run.sh
```

也可以分两步启动：

```bash
docker compose up -d --wait db
cd backend
mvn compile exec:java
```

打开 <http://localhost:8080/tamo/>。终端按 Ctrl+C 停止 Java 服务。
数据库可以继续运行；需要停止时，在项目根目录执行：

```bash
docker compose stop db
```

数据库使用 Docker 命名卷 `tamo-local_postgres_data` 保存数据。
停止容器、重启 Java 或重启电脑不会删除数据；重新打开 Docker 并运行启动命令即可。
`docker compose down` 也会保留命名卷。不要使用 `docker compose down -v`，它会删除数据库卷。

## 迁移与数据位置

首次连接新数据库时，应用自动执行 `backend/src/main/resources/db/V1__initial.sql`，
并导入当前 `backend/src/main/resources/data.json`。
迁移前会在项目根目录 `backups/` 下复制一份带时间戳的 JSON 备份。
整个建表和导入过程使用事务：任何一步失败都不会留下已完成的迁移标记。

迁移完成后，PostgreSQL 是数据来源。后续注册、修改、删除只写数据库，
不再同步回 `data.json`，重启也不会重新导入或用 JSON 覆盖数据库。
因此不要再通过编辑或 Git 恢复 `data.json` 来重置正在使用的数据库。
原始 JSON 和文件上传目录都会保留。

用户、资料、课程、职位、申请、面试、通知、学期、部门、日志、工作量分配、消息分别存表。
标量字段使用普通 SQL 列，已有嵌套列表（如资料中的技能、经历、附件信息）使用 JSONB 列。
主键、邮箱唯一索引和主要外键在数据库中校验。
申请周期功能通过 V2 自动增加 application_periods 表和申请的 period_id 关联。已有申请归入关闭的历史周期，详见 [周期管理说明](APPLICATION_PERIODS_zh.md)。
V3 增加 weekly_availabilities 表和申请时间安排快照，详见 [每周可用时间说明](WEEKLY_AVAILABILITY_zh.md)。
V4 增加 course_sections 表，保存课程 Section、每周上课时间和 TA 需求，详见 [课程 Section 说明](COURSE_SECTIONS_zh.md)。
后续数据库结构变化应增加新的迁移，不要修改已执行过的 V1 / V2 / V3 / V4。

保留 `JsonDataService` 类名以兼容现有业务代码，它实际已使用 JDBC，名称不代表仍在读写 JSON 文件。
现有业务服务仍通过内存模型处理单个请求，每次 API 请求从数据库刷新，并只保存变化的记录。
请求使用事务和应用专用的 PostgreSQL advisory lock，串行处理现有共享模型的修改，
防止两个请求或两个应用实例同时覆盖彼此的数据。
此实现适合当前小规模项目；更大规模并发时应继续改为各业务服务直接查询和更新数据库。

## 连接参数

| 参数 | 默认值 |
|---|---|
| 主机 | `127.0.0.1` |
| 端口 | `5433`（容器内部为 `5432`） |
| 数据库 | `tamo` |
| 用户 | `tamo` |
| 本地开发密码 | `tamo_local_only` |
| JDBC URL | `jdbc:postgresql://127.0.0.1:5433/tamo` |

仅绑定本机地址。可通过 DBeaver、DataGrip 或下列命令查看数据库：

```bash
docker compose exec db psql -U tamo -d tamo
```

进入后可运行 `\dt` 列出表，或 `SELECT id, name, email, role FROM users;` 查看账号。

后端支持环境变量 `TAMO_DB_URL`、`TAMO_DB_USER` 和 `TAMO_DB_PASSWORD`。
Compose 支持 `TAMO_DB_PORT` 和 `TAMO_DB_PASSWORD`。
例如端口冲突时，在启动前执行：

```bash
export TAMO_DB_PORT=5434
export TAMO_DB_URL=jdbc:postgresql://127.0.0.1:5434/tamo
./run.sh
```

密码仅用于本地开发。数据库已有数据卷后，更改环境变量不会自动修改数据库角色的密码，
需要先使用 PostgreSQL 的 `\password tamo` 修改，再更新后端环境变量。
已有账号密码的验证逻辑此次保持兼容；密码哈希改造属于后续单独工作。

## 备份和恢复

在项目根目录备份数据库（每次使用不同文件名）：

```bash
mkdir -p backups
docker compose exec -T db pg_dump -U tamo -d tamo -Fc > backups/tamo-backup.dump
```

附件文件仍在 `backend/src/main/webapp/static/uploads/`，应另行备份该目录。

验证备份目录：

```bash
docker compose exec -T db pg_restore --list < backups/tamo-backup.dump
```

恢复建议先恢复到另一个空数据库，验证后再使用。以下示例创建 `tamo_restore`：

```bash
docker compose exec db createdb -U tamo tamo_restore
docker compose exec -T db pg_restore -U tamo -d tamo_restore --exit-on-error < backups/tamo-backup.dump
```

停止 Java 服务后，在 `backend` 目录启动恢复的数据库对应的应用：

```bash
TAMO_DB_URL=jdbc:postgresql://127.0.0.1:5433/tamo_restore mvn compile exec:java
```

## 测试

现有单元测试不需要启动数据库：在 `backend` 目录运行 `mvn test`。

真实 PostgreSQL 集成测试使用独立数据库和临时 schema，不修改应用数据。
先在根目录创建一次测试数据库：

```bash
docker compose exec db createdb -U tamo tamo_test
```

然后运行：

```bash
cd backend
TAMO_TEST_DB_URL=jdbc:postgresql://127.0.0.1:5433/tamo_test mvn test
```

集成测试覆盖迁移、重启后读取、修改与删除、重复邮箱、失败回滚，以及事务间的相互隔离。

参考：[PostgreSQL 事务与锁](https://www.postgresql.org/docs/17/explicit-locking.html)、
[官方 JDBC 驱动](https://jdbc.postgresql.org/)。


V5 新增 `section_assignments`，记录正式分配、Section 快照与取消历史。操作见 [TA 分配说明](TA_ASSIGNMENTS_zh.md)。


V6 为正式分配增加发布和学生回复字段。已有记录保留为未发布草稿，不自动发送通知。详见 [发布与回复](OFFER_RESPONSES_zh.md)。


V7 增加回复截止时间和逾期记录；已有有效待回复结果在升级时获得 7 天宽限期。参见 [回复截止时间](REPLY_DEADLINES_zh.md)。
