#!/bin/bash
# TA&MO 后端 API 自动化测试脚本
# 对应 TEST_DOCUMENT.md 中的核心测试用例

BASE="http://localhost:8080/tamo"
COOKIE_JAR="/tmp/tamo_cookies.txt"
PASS=0
FAIL=0
TOTAL=0

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

assert_status() {
    local test_id="$1"
    local desc="$2"
    local expected="$3"
    local actual="$4"
    local body="$5"
    TOTAL=$((TOTAL + 1))
    if [ "$expected" = "$actual" ]; then
        PASS=$((PASS + 1))
        echo -e "${GREEN}✓ ${test_id} ${desc} [${actual}]${NC}"
    else
        FAIL=$((FAIL + 1))
        echo -e "${RED}✗ ${test_id} ${desc} [期望:${expected} 实际:${actual}]${NC}"
        echo -e "  响应: ${body}" | head -c 200
        echo ""
    fi
}

assert_body_contains() {
    local test_id="$1"
    local desc="$2"
    local expected_text="$3"
    local body="$4"
    local status="$5"
    TOTAL=$((TOTAL + 1))
    if echo "$body" | grep -q "$expected_text"; then
        PASS=$((PASS + 1))
        echo -e "${GREEN}✓ ${test_id} ${desc} [${status}]${NC}"
    else
        FAIL=$((FAIL + 1))
        echo -e "${RED}✗ ${test_id} ${desc} - 响应不包含'${expected_text}' [${status}]${NC}"
        echo -e "  响应: ${body}" | head -c 200
        echo ""
    fi
}

assert_body_not_contains() {
    local test_id="$1"
    local desc="$2"
    local unwanted_text="$3"
    local body="$4"
    local status="$5"
    TOTAL=$((TOTAL + 1))
    if echo "$body" | grep -q "$unwanted_text"; then
        FAIL=$((FAIL + 1))
        echo -e "${RED}✗ ${test_id} ${desc} - 响应不应包含'${unwanted_text}' [${status}]${NC}"
    else
        PASS=$((PASS + 1))
        echo -e "${GREEN}✓ ${test_id} ${desc} [${status}]${NC}"
    fi
}

login() {
    local email="$1"
    local password="$2"
    local role="$3"
    local cookie_name="$4"
    curl -s -c "/tmp/tamo_${cookie_name}.txt" -o /dev/null \
        -X POST "$BASE/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"${email}\",\"password\":\"${password}\",\"role\":\"${role}\"}"
}

echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  TA&MO 后端 API 自动化测试${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""

# =====================================================
# 二、公共认证 API 测试
# =====================================================
echo -e "${YELLOW}--- 二、公共认证 API 测试 ---${NC}"

# AUTH-001: 正常登录 applicant
RESP=$(curl -s -w "\n%{http_code}" -c /tmp/tamo_u001.txt -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"alex.j@university.edu","password":"hashed_password_1","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-001" "正常登录(applicant)" "200" "$STATUS" "$BODY"

# AUTH-002: 正常登录 organiser
RESP=$(curl -s -w "\n%{http_code}" -c /tmp/tamo_u005.txt -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"alan@university.edu","password":"hashed_password_5","role":"organiser"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-002" "正常登录(organiser)" "200" "$STATUS" "$BODY"

# AUTH-003: 正常登录 admin
RESP=$(curl -s -w "\n%{http_code}" -c /tmp/tamo_u010.txt -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@university.edu","password":"hashed_password_10","role":"admin"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-003" "正常登录(admin)" "200" "$STATUS" "$BODY"

# AUTH-004: 密码错误
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"alex.j@university.edu","password":"wrong_password","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-004" "密码错误" "401" "$STATUS" "$BODY"
assert_body_contains "AUTH-004b" "错误信息" "邮箱或密码错误" "$BODY" "$STATUS"

# AUTH-005: 邮箱不存在
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"nonexist@university.edu","password":"any","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-005" "邮箱不存在" "401" "$STATUS" "$BODY"

# AUTH-006: 角色不匹配
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"alex.j@university.edu","password":"hashed_password_1","role":"organiser"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-006" "角色不匹配" "403" "$STATUS" "$BODY"
assert_body_contains "AUTH-006b" "错误信息" "角色不匹配" "$BODY" "$STATUS"

# AUTH-007: 缺少 email
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"password":"123","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-007" "缺少email字段" "400" "$STATUS" "$BODY"
assert_body_contains "AUTH-007b" "错误信息" "缺少必填字段" "$BODY" "$STATUS"

# AUTH-008: 缺少 password
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"alex.j@university.edu","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-008" "缺少password字段" "400" "$STATUS" "$BODY"

# AUTH-009: 账户被挂起
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"carol.w@university.edu","password":"hashed_password_3","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-009" "账户被挂起" "403" "$STATUS" "$BODY"
assert_body_contains "AUTH-009b" "错误信息" "账户已被挂起" "$BODY" "$STATUS"

# AUTH-010: 正常注册
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"name":"New User","email":"new@university.edu","password":"Secure123!","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-010" "正常注册" "201" "$STATUS" "$BODY"

# AUTH-011: 邮箱已注册
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"name":"Dup User","email":"alex.j@university.edu","password":"Secure123!","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-011" "邮箱已注册" "409" "$STATUS" "$BODY"
assert_body_contains "AUTH-011b" "错误信息" "邮箱已被注册" "$BODY" "$STATUS"

# AUTH-012: 缺少 name
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"email":"x@university.edu","password":"Secure123!","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-012" "缺少name字段" "400" "$STATUS" "$BODY"

# AUTH-013: 邮箱格式不合法
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test","email":"not-an-email","password":"Secure123!","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-013" "邮箱格式不合法" "400" "$STATUS" "$BODY"
assert_body_contains "AUTH-013b" "错误信息" "邮箱格式不正确" "$BODY" "$STATUS"

# AUTH-014: role 不合法
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test","email":"y@university.edu","password":"Secure123!","role":"superadmin"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-014" "role值不合法" "400" "$STATUS" "$BODY"
assert_body_contains "AUTH-014b" "错误信息" "角色值不合法" "$BODY" "$STATUS"

# AUTH-015: 正常退出
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X POST "$BASE/api/auth/logout")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-015" "正常退出" "200" "$STATUS" "$BODY"
assert_body_contains "AUTH-015b" "退出成功" "退出成功" "$BODY" "$STATUS"

# 重新登录 u001
login "alex.j@university.edu" "hashed_password_1" "applicant" "u001"

# AUTH-017: 获取当前用户信息
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/auth/me")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-017" "获取当前用户(已登录)" "200" "$STATUS" "$BODY"

# AUTH-018: 未登录访问 /me
RESP=$(curl -s -w "\n%{http_code}" "$BASE/api/auth/me")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "AUTH-018" "未登录访问/me" "401" "$STATUS" "$BODY"
assert_body_contains "AUTH-018b" "错误信息" "未登录" "$BODY" "$STATUS"

# AUTH-020: 返回数据不包含密码
RESP=$(curl -s -b /tmp/tamo_u001.txt "$BASE/api/auth/me")
assert_body_not_contains "AUTH-020" "不包含password" "password" "$RESP" "200"

# =====================================================
# 三、Applicant API 测试
# =====================================================
echo ""
echo -e "${YELLOW}--- 三、Applicant API 测试 ---${NC}"

# 重新登录各角色
login "alex.j@university.edu" "hashed_password_1" "applicant" "u001"
login "bob.s@university.edu" "hashed_password_2" "applicant" "u002"
login "alan@university.edu" "hashed_password_5" "organiser" "u005"
login "admin@university.edu" "hashed_password_10" "admin" "u010"

# APP-001: 获取仪表盘
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/dashboard")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-001" "获取仪表盘(u001)" "200" "$STATUS" "$BODY"
assert_body_contains "APP-001b" "包含activeApplications" "activeApplications" "$BODY" "$STATUS"

# APP-003: 未登录访问
RESP=$(curl -s -w "\n%{http_code}" "$BASE/api/applicant/dashboard")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-003" "未登录访问仪表盘" "401" "$STATUS" "$BODY"

# APP-004: 非 applicant 角色
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt "$BASE/api/applicant/dashboard")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-004" "organiser访问applicant接口" "403" "$STATUS" "$BODY"
assert_body_contains "APP-004b" "错误信息" "无权限访问" "$BODY" "$STATUS"

# APP-005: 获取职位列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/jobs")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-005" "获取职位列表" "200" "$STATUS" "$BODY"

# APP-010: 搜索无结果
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/jobs?keyword=NonExistentJob12345")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-010" "搜索无结果" "200" "$STATUS" "$BODY"
assert_body_contains "APP-010b" "返回空数组" "\[\]" "$BODY" "$STATUS"

# APP-012: 获取职位详情
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/jobs/j001")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-012" "获取职位详情(j001)" "200" "$STATUS" "$BODY"

# APP-013: 职位ID不存在
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/jobs/j999")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-013" "职位不存在(j999)" "404" "$STATUS" "$BODY"
assert_body_contains "APP-013b" "错误信息" "职位不存在" "$BODY" "$STATUS"

# APP-014: 查看已关闭的职位
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/jobs/j002")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-014" "查看已关闭职位(j002)" "200" "$STATUS" "$BODY"
assert_body_contains "APP-014b" "状态为closed" "closed" "$BODY" "$STATUS"

# APP-016: 重复申请
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X POST "$BASE/api/applicant/jobs/j001/apply" \
    -H "Content-Type: application/json" \
    -d '{"coverLetter":"...","documentIds":[]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-016" "重复申请同一岗位" "409" "$STATUS" "$BODY"
assert_body_contains "APP-016b" "错误信息" "已申请过该岗位" "$BODY" "$STATUS"

# APP-017: 向已关闭岗位申请
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X POST "$BASE/api/applicant/jobs/j002/apply" \
    -H "Content-Type: application/json" \
    -d '{"coverLetter":"...","documentIds":[]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-017" "申请已关闭岗位" "400" "$STATUS" "$BODY"
assert_body_contains "APP-017b" "错误信息" "该岗位已关闭" "$BODY" "$STATUS"

# APP-018: 缺少 coverLetter
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X POST "$BASE/api/applicant/jobs/j003/apply" \
    -H "Content-Type: application/json" \
    -d '{"documentIds":[]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-018" "缺少coverLetter" "400" "$STATUS" "$BODY"
assert_body_contains "APP-018b" "错误信息" "缺少必填字段" "$BODY" "$STATUS"

# APP-019: 岗位ID不存在
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X POST "$BASE/api/applicant/jobs/j999/apply" \
    -H "Content-Type: application/json" \
    -d '{"coverLetter":"...","documentIds":[]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-019" "申请不存在的岗位" "404" "$STATUS" "$BODY"

# APP-015: 正常提交申请(u002)
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u002.txt -X POST "$BASE/api/applicant/jobs/j001/apply" \
    -H "Content-Type: application/json" \
    -d '{"coverLetter":"Dear Committee, I am interested...","documentIds":["doc002"]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-015" "正常提交申请(u002->j001)" "201" "$STATUS" "$BODY"
assert_body_contains "APP-015b" "状态为applied" "applied" "$BODY" "$STATUS"

# APP-020: 获取申请列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/applications")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-020" "获取申请列表" "200" "$STATUS" "$BODY"

# APP-024: 获取申请详情
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/applications/a001")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-024" "获取申请详情(a001)" "200" "$STATUS" "$BODY"

# APP-025: 申请不存在
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/applications/a999")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-025" "申请不存在(a999)" "404" "$STATUS" "$BODY"

# APP-026: 访问其他用户的申请
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u002.txt "$BASE/api/applicant/applications/a001")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-026" "访问他人申请(403)" "403" "$STATUS" "$BODY"
assert_body_contains "APP-026b" "错误信息" "无权限访问该申请" "$BODY" "$STATUS"

# APP-027: 获取个人资料
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/applicant/profile")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-027" "获取个人资料" "200" "$STATUS" "$BODY"

# APP-032: GPA超出范围
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X PUT "$BASE/api/applicant/profile" \
    -H "Content-Type: application/json" \
    -d '{"gpa":5.0,"maxGpa":4.0}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-032" "GPA超出范围" "400" "$STATUS" "$BODY"
assert_body_contains "APP-032b" "错误信息" "GPA 不能超过最大值" "$BODY" "$STATUS"

# APP-038: 删除文档
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X DELETE "$BASE/api/applicant/profile/documents/doc001")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-038" "删除文档(doc001)" "200" "$STATUS" "$BODY"
assert_body_contains "APP-038b" "删除成功" "文档删除成功" "$BODY" "$STATUS"

# APP-039: 文档不存在
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt -X DELETE "$BASE/api/applicant/profile/documents/doc999")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "APP-039" "文档不存在(doc999)" "404" "$STATUS" "$BODY"

# APP-041: organiser 访问 applicant 接口
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt "$BASE/api/applicant/dashboard")
STATUS=$(echo "$RESP" | tail -1)
assert_status "APP-041" "organiser访问applicant接口" "403" "$STATUS"

# APP-042: admin 访问 applicant 接口
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/applicant/jobs")
STATUS=$(echo "$RESP" | tail -1)
assert_status "APP-042" "admin访问applicant接口" "403" "$STATUS"

# =====================================================
# 四、Organiser API 测试
# =====================================================
echo ""
echo -e "${YELLOW}--- 四、Organiser API 测试 ---${NC}"

# ORG-001: 获取仪表盘
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt "$BASE/api/organiser/dashboard")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-001" "获取组织者仪表盘" "200" "$STATUS" "$BODY"
assert_body_contains "ORG-001b" "包含activePostings" "activePostings" "$BODY" "$STATUS"

# ORG-004: applicant 访问
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/organiser/dashboard")
STATUS=$(echo "$RESP" | tail -1)
assert_status "ORG-004" "applicant访问organiser接口" "403" "$STATUS"

# ORG-005: 获取岗位列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt "$BASE/api/organiser/jobs")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-005" "获取岗位列表(u005)" "200" "$STATUS" "$BODY"

# ORG-007: 创建新岗位
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X POST "$BASE/api/organiser/jobs" \
    -H "Content-Type: application/json" \
    -d '{"moduleId":"m001","moduleCode":"CS201","title":"TA - CS201 Lab","type":"Part-time","hoursPerWeek":8,"positions":2,"deadline":"2026-11-15","tags":["Java"],"requirements":["A- in CS201"],"responsibilities":["Grade assignments"]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-007" "创建新岗位" "201" "$STATUS" "$BODY"
assert_body_contains "ORG-007b" "状态为active" "active" "$BODY" "$STATUS"

# ORG-008: 缺少 title
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X POST "$BASE/api/organiser/jobs" \
    -H "Content-Type: application/json" \
    -d '{"moduleId":"m001","type":"Part-time","hoursPerWeek":8}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-008" "缺少title字段" "400" "$STATUS" "$BODY"
assert_body_contains "ORG-008b" "错误信息" "缺少必填字段" "$BODY" "$STATUS"

# ORG-012: 编辑其他组织者的岗位
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X PUT "$BASE/api/organiser/jobs/j003" \
    -H "Content-Type: application/json" \
    -d '{"title":"Hacked"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-012" "编辑他人岗位(403)" "403" "$STATUS" "$BODY"
assert_body_contains "ORG-012b" "错误信息" "无权限编辑该岗位" "$BODY" "$STATUS"

# ORG-013: 岗位不存在
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X PUT "$BASE/api/organiser/jobs/j999" \
    -H "Content-Type: application/json" \
    -d '{"title":"Test"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-013" "岗位不存在(j999)" "404" "$STATUS" "$BODY"

# ORG-015: 删除其他组织者的岗位
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X DELETE "$BASE/api/organiser/jobs/j003")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-015" "删除他人岗位(403)" "403" "$STATUS" "$BODY"
assert_body_contains "ORG-015b" "错误信息" "无权限删除该岗位" "$BODY" "$STATUS"

# ORG-017: 获取 pipeline
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt "$BASE/api/organiser/pipeline")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-017" "获取pipeline" "200" "$STATUS" "$BODY"

# ORG-026: 不合法 status
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X PUT "$BASE/api/organiser/pipeline/a001/status" \
    -H "Content-Type: application/json" \
    -d '{"status":"invalid_status"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-026" "不合法status值" "400" "$STATUS" "$BODY"
assert_body_contains "ORG-026b" "错误信息" "状态值不合法" "$BODY" "$STATUS"

# ORG-021: 更新状态 -> review
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X PUT "$BASE/api/organiser/pipeline/a001/status" \
    -H "Content-Type: application/json" \
    -d '{"status":"review"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-021" "更新状态->review" "200" "$STATUS" "$BODY"

# ORG-034: 不合法 action
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X PUT "$BASE/api/organiser/applications/a001/action" \
    -H "Content-Type: application/json" \
    -d '{"action":"delete"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-034" "不合法action" "400" "$STATUS" "$BODY"
assert_body_contains "ORG-034b" "错误信息" "操作类型不合法" "$BODY" "$STATUS"

# ORG-035: 缺少 action
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X PUT "$BASE/api/organiser/applications/a001/action" \
    -H "Content-Type: application/json" \
    -d '{}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-035" "缺少action字段" "400" "$STATUS" "$BODY"

# ORG-036: 获取面试列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt "$BASE/api/organiser/interviews")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-036" "获取面试列表(u005)" "200" "$STATUS" "$BODY"

# ORG-039: 缺少 applicationId
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X POST "$BASE/api/organiser/interviews" \
    -H "Content-Type: application/json" \
    -d '{"candidateId":"u001","date":"2026-11-01","startTime":"14:00"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-039" "缺少applicationId" "400" "$STATUS" "$BODY"

# ORG-041: type 不合法
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X POST "$BASE/api/organiser/interviews" \
    -H "Content-Type: application/json" \
    -d '{"applicationId":"a001","candidateId":"u001","date":"2026-11-01","startTime":"14:00","endTime":"14:30","type":"phone"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-041" "面试类型不合法(phone)" "400" "$STATUS" "$BODY"
assert_body_contains "ORG-041b" "错误信息" "面试类型不合法" "$BODY" "$STATUS"

# ORG-038: 正常创建面试
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u005.txt -X POST "$BASE/api/organiser/interviews" \
    -H "Content-Type: application/json" \
    -d '{"applicationId":"a001","candidateId":"u001","candidateName":"Alex Johnson","moduleCode":"CS201","moduleName":"Data Structures","date":"2026-11-01","startTime":"14:00","endTime":"14:30","type":"video"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ORG-038" "创建面试" "201" "$STATUS" "$BODY"

# ORG-048: applicant 访问 organiser
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/organiser/dashboard")
STATUS=$(echo "$RESP" | tail -1)
assert_status "ORG-048" "applicant访问organiser接口" "403" "$STATUS"

# ORG-050: 未登录访问
RESP=$(curl -s -w "\n%{http_code}" "$BASE/api/organiser/pipeline")
STATUS=$(echo "$RESP" | tail -1)
assert_status "ORG-050" "未登录访问organiser接口" "401" "$STATUS"

# =====================================================
# 五、Admin API 测试
# =====================================================
echo ""
echo -e "${YELLOW}--- 五、Admin API 测试 ---${NC}"

# ADM-001: 获取平台数据
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/dashboard")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-001" "获取平台数据" "200" "$STATUS" "$BODY"
assert_body_contains "ADM-001b" "包含totalApplicants" "totalApplicants" "$BODY" "$STATUS"

# ADM-003: applicant 访问 admin
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/admin/dashboard")
STATUS=$(echo "$RESP" | tail -1)
assert_status "ADM-003" "applicant访问admin接口" "403" "$STATUS"

# ADM-005: 获取用户列表（不含密码）
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/users")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-005" "获取用户列表" "200" "$STATUS" "$BODY"
assert_body_not_contains "ADM-005b" "不含password" "password" "$BODY" "$STATUS"

# ADM-011: 创建用户
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/users" \
    -H "Content-Type: application/json" \
    -d '{"name":"New Faculty","email":"faculty@university.edu","role":"organiser","department":"Mathematics"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-011" "创建用户" "201" "$STATUS" "$BODY"

# ADM-012: 邮箱已占用
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/users" \
    -H "Content-Type: application/json" \
    -d '{"name":"Dup","email":"alex.j@university.edu","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-012" "邮箱已占用" "409" "$STATUS" "$BODY"

# ADM-013: 缺少 name
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/users" \
    -H "Content-Type: application/json" \
    -d '{"email":"x@university.edu","role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-013" "缺少name字段" "400" "$STATUS" "$BODY"

# ADM-015: role 不合法
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/users" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test","email":"t@university.edu","role":"superuser"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-015" "role值不合法" "400" "$STATUS" "$BODY"
assert_body_contains "ADM-015b" "错误信息" "角色值不合法" "$BODY" "$STATUS"

# ADM-020: 修改自己
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X PUT "$BASE/api/admin/users/u010" \
    -H "Content-Type: application/json" \
    -d '{"role":"applicant"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-020" "修改自己角色" "400" "$STATUS" "$BODY"
assert_body_contains "ADM-020b" "错误信息" "不能修改自己的角色" "$BODY" "$STATUS"

# ADM-023: 删除自己
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X DELETE "$BASE/api/admin/users/u010")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-023" "删除自己账户" "400" "$STATUS" "$BODY"
assert_body_contains "ADM-023b" "错误信息" "不能删除自己的账户" "$BODY" "$STATUS"

# ADM-024: 获取模块列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/modules")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-024" "获取模块列表" "200" "$STATUS" "$BODY"

# ADM-028: 创建模块缺少 code
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/modules" \
    -H "Content-Type: application/json" \
    -d '{"name":"ML","department":"CS"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-028" "缺少code字段" "400" "$STATUS" "$BODY"

# ADM-030: 模块代码重复
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/modules" \
    -H "Content-Type: application/json" \
    -d '{"code":"CS201","name":"Dup Module","department":"CS"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-030" "模块代码重复" "409" "$STATUS" "$BODY"
assert_body_contains "ADM-030b" "错误信息" "模块代码已存在" "$BODY" "$STATUS"

# ADM-035: 删除有关联岗位的模块
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X DELETE "$BASE/api/admin/modules/m001")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-035" "删除有关联岗位的模块" "400" "$STATUS" "$BODY"
assert_body_contains "ADM-035b" "错误信息" "关联岗位" "$BODY" "$STATUS"

# ADM-036: 获取学期列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/properties/terms")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-036" "获取学期列表" "200" "$STATUS" "$BODY"

# ADM-038: 创建学期缺少 name
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/properties/terms" \
    -H "Content-Type: application/json" \
    -d '{"startDate":"2027-01-15","endDate":"2027-05-20"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-038" "缺少学期name字段" "400" "$STATUS" "$BODY"

# ADM-039: 开始日期晚于结束日期
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/properties/terms" \
    -H "Content-Type: application/json" \
    -d '{"name":"Invalid","startDate":"2027-12-01","endDate":"2027-01-01"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-039" "开始日期晚于结束日期" "400" "$STATUS" "$BODY"
assert_body_contains "ADM-039b" "错误信息" "开始日期不能晚于结束日期" "$BODY" "$STATUS"

# ADM-040: 学期名称重复
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/properties/terms" \
    -H "Content-Type: application/json" \
    -d '{"name":"Fall 2026","startDate":"2026-08-15","endDate":"2026-12-20"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-040" "学期名称重复" "409" "$STATUS" "$BODY"
assert_body_contains "ADM-040b" "错误信息" "学期名称已存在" "$BODY" "$STATUS"

# ADM-041: 获取院系列表
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/properties/departments")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-041" "获取院系列表" "200" "$STATUS" "$BODY"

# ADM-043: 院系缺少 name
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/properties/departments" \
    -H "Content-Type: application/json" \
    -d '{}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-043" "缺少院系name字段" "400" "$STATUS" "$BODY"

# ADM-044: 院系名称重复
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X POST "$BASE/api/admin/properties/departments" \
    -H "Content-Type: application/json" \
    -d '{"name":"Computer Science"}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-044" "院系名称重复" "409" "$STATUS" "$BODY"
assert_body_contains "ADM-044b" "错误信息" "院系名称已存在" "$BODY" "$STATUS"

# ADM-045: 获取日志
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/logs")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-045" "获取系统日志" "200" "$STATUS" "$BODY"

# ADM-050: 获取工作量
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt "$BASE/api/admin/workload")
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-050" "获取工作量数据" "200" "$STATUS" "$BODY"

# ADM-055: TA 用户不存在
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u010.txt -X PUT "$BASE/api/admin/workload/u999/reassign" \
    -H "Content-Type: application/json" \
    -d '{"removeModules":["CS201"]}')
STATUS=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "ADM-055" "TA用户不存在" "404" "$STATUS" "$BODY"
assert_body_contains "ADM-055b" "错误信息" "TA 用户不存在" "$BODY" "$STATUS"

# ADM-057: applicant 访问 admin
RESP=$(curl -s -w "\n%{http_code}" -b /tmp/tamo_u001.txt "$BASE/api/admin/dashboard")
STATUS=$(echo "$RESP" | tail -1)
assert_status "ADM-057" "applicant访问admin接口" "403" "$STATUS"

# ADM-059: 未登录访问
RESP=$(curl -s -w "\n%{http_code}" "$BASE/api/admin/dashboard")
STATUS=$(echo "$RESP" | tail -1)
assert_status "ADM-059" "未登录访问admin接口" "401" "$STATUS"

# =====================================================
# 六、非功能测试
# =====================================================
echo ""
echo -e "${YELLOW}--- 六、非功能测试 ---${NC}"

# NF-001: CORS 头验证
RESP=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS "$BASE/api/auth/login" \
    -H "Origin: http://localhost:5173")
assert_status "NF-001" "OPTIONS预检请求" "200" "$RESP"

CORS_HEADER=$(curl -s -I -X OPTIONS "$BASE/api/auth/login" 2>&1 | grep -i "access-control-allow-origin")
TOTAL=$((TOTAL + 1))
if echo "$CORS_HEADER" | grep -q "localhost:5173"; then
    PASS=$((PASS + 1))
    echo -e "${GREEN}✓ NF-001b CORS头包含localhost:5173${NC}"
else
    FAIL=$((FAIL + 1))
    echo -e "${RED}✗ NF-001b CORS头不包含localhost:5173${NC}"
fi

# NF-004: JSON 响应格式
CONTENT_TYPE=$(curl -s -I -b /tmp/tamo_u001.txt "$BASE/api/applicant/dashboard" | grep -i "content-type")
TOTAL=$((TOTAL + 1))
if echo "$CONTENT_TYPE" | grep -q "application/json"; then
    PASS=$((PASS + 1))
    echo -e "${GREEN}✓ NF-004  Content-Type为application/json${NC}"
else
    FAIL=$((FAIL + 1))
    echo -e "${RED}✗ NF-004  Content-Type不正确: ${CONTENT_TYPE}${NC}"
fi

# =====================================================
# 汇总
# =====================================================
echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  测试结果汇总${NC}"
echo -e "${YELLOW}============================================${NC}"
echo -e "  总用例数: ${TOTAL}"
echo -e "  ${GREEN}通过: ${PASS}${NC}"
echo -e "  ${RED}失败: ${FAIL}${NC}"
echo -e "  通过率: $(echo "scale=1; $PASS * 100 / $TOTAL" | bc)%"
echo -e "${YELLOW}============================================${NC}"

# 清理 cookie 文件
rm -f /tmp/tamo_u001.txt /tmp/tamo_u002.txt /tmp/tamo_u005.txt /tmp/tamo_u006.txt /tmp/tamo_u010.txt

if [ $FAIL -gt 0 ]; then
    exit 1
else
    exit 0
fi
