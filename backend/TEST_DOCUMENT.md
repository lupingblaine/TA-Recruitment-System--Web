# TA&MO 后端 API 完整测试文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 项目名称 | TA&MO（Teaching Assistant & Module Organiser） |
| 文档版本 | v1.0 |
| 编写日期 | 2026-04-11 |
| 接口总数 | 42 个（Auth 4 + Applicant 10 + Organiser 12 + Admin 16） |
| 测试用例总数 | 197 个 |
| 覆盖率 | 100%（42/42 接口） |

---

## 一、测试环境与前置条件

### 1.1 测试环境

| 项目 | 说明 |
|------|------|
| 服务端框架 | Java Servlet / JSP |
| 基础 URL | `http://localhost:8080` |
| 数据存储 | `data.json` 文件（代替数据库） |
| Session 管理 | Java Servlet HttpSession |
| 数据格式 | 请求/响应均为 `application/json` |
| 跨域处理 | CorsFilter |
| 认证拦截 | AuthFilter（拦截 `/api/applicant/*`、`/api/organiser/*`、`/api/admin/*`） |

### 1.2 测试用例格式说明

每条测试用例包含以下字段：

| 字段 | 说明 |
|------|------|
| 编号 | 格式为 `模块缩写-序号`，如 `AUTH-001` |
| 描述 | 测试目的简述 |
| 前置条件 | 执行测试前需要满足的状态 |
| 请求方法 | HTTP Method |
| 请求路径 | API 路径 |
| 请求参数/Body | 请求携带的数据 |
| 预期状态码 | HTTP 响应状态码 |
| 预期响应 | 响应 Body 的关键字段 |
| 测试结果 | 通过/未通过（执行时填写） |

### 1.3 预置测试数据

测试前 `data.json` 中应包含以下基础数据：

**用户数据（users）**：

| id | name | email | password | role | status |
|----|------|-------|----------|------|--------|
| u001 | Alex Johnson | alex.j@university.edu | hashed_password_1 | applicant | active |
| u002 | Bob Smith | bob.s@university.edu | hashed_password_2 | applicant | active |
| u003 | Carol White | carol.w@university.edu | hashed_password_3 | applicant | suspended |
| u005 | Dr. Alan Turing | alan@university.edu | hashed_password_5 | organiser | active |
| u006 | Dr. Grace Hopper | grace@university.edu | hashed_password_6 | organiser | active |
| u010 | Admin Root | admin@university.edu | hashed_password_10 | admin | active |

**档案数据（profiles）**：u001 对应 p001，含 documents: [doc001]

**模块数据（modules）**：m001（CS201，organiserId: u005）、m002（CS301，organiserId: u006）

**岗位数据（jobs）**：j001（active, moduleId: m001, organiserId: u005）、j002（closed, organiserId: u005）、j003（active, organiserId: u006）

**申请数据（applications）**：a001（applicantId: u001, jobId: j001, status: interview）

**面试数据（interviews）**：i001（applicationId: a001, candidateId: u001, organiserId: u005, status: upcoming）

**学期数据（terms）**：t001（Fall 2026, active）

**院系数据（departments）**：d001（Computer Science）

**日志数据（logs）**：至少 5 条不同 type 的日志

**工作量数据（workloadAssignments）**：w001（taUserId: u001, moduleCode: CS201）

---

## 二、公共认证 API 测试（4 个接口，20 个用例）

### 2.1 POST `/api/auth/login` — 用户登录

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| AUTH-001 | 正常登录（applicant 角色） | 用户 u001 存在且状态为 active | `{"email":"alex.j@university.edu","password":"hashed_password_1","role":"applicant"}` | 200 | 返回用户信息（id, name, email, role），创建 Session | |
| AUTH-002 | 正常登录（organiser 角色） | 用户 u005 存在且状态为 active | `{"email":"alan@university.edu","password":"hashed_password_5","role":"organiser"}` | 200 | 返回用户信息，role 为 organiser | |
| AUTH-003 | 正常登录（admin 角色） | 用户 u010 存在且状态为 active | `{"email":"admin@university.edu","password":"hashed_password_10","role":"admin"}` | 200 | 返回用户信息，role 为 admin | |
| AUTH-004 | 密码错误 | 用户 u001 存在 | `{"email":"alex.j@university.edu","password":"wrong_password","role":"applicant"}` | 401 | `{"error":"邮箱或密码错误"}` | |
| AUTH-005 | 邮箱不存在 | 无 | `{"email":"nonexist@university.edu","password":"any","role":"applicant"}` | 401 | `{"error":"邮箱或密码错误"}` | |
| AUTH-006 | 角色不匹配（applicant 邮箱以 organiser 角色登录） | 用户 u001 为 applicant | `{"email":"alex.j@university.edu","password":"hashed_password_1","role":"organiser"}` | 403 | `{"error":"角色不匹配"}` | |
| AUTH-007 | 缺少 email 字段 | 无 | `{"password":"123","role":"applicant"}` | 400 | `{"error":"缺少必填字段"}` | |
| AUTH-008 | 缺少 password 字段 | 无 | `{"email":"alex.j@university.edu","role":"applicant"}` | 400 | `{"error":"缺少必填字段"}` | |
| AUTH-009 | 账户被挂起 | 用户 u003 状态为 suspended | `{"email":"carol.w@university.edu","password":"hashed_password_3","role":"applicant"}` | 403 | `{"error":"账户已被挂起"}` | |

### 2.2 POST `/api/auth/signup` — 用户注册

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| AUTH-010 | 正常注册 | 邮箱未被占用 | `{"name":"New User","email":"new@university.edu","password":"Secure123!","role":"applicant"}` | 201 | 返回新用户信息（含自动生成的 id），data.json 中 users 新增一条记录 | |
| AUTH-011 | 邮箱已被注册 | 用户 u001 已存在 | `{"name":"Dup User","email":"alex.j@university.edu","password":"Secure123!","role":"applicant"}` | 409 | `{"error":"邮箱已被注册"}` | |
| AUTH-012 | 缺少 name 字段 | 无 | `{"email":"x@university.edu","password":"Secure123!","role":"applicant"}` | 400 | `{"error":"缺少必填字段"}` | |
| AUTH-013 | 邮箱格式不合法 | 无 | `{"name":"Test","email":"not-an-email","password":"Secure123!","role":"applicant"}` | 400 | `{"error":"邮箱格式不正确"}` | |
| AUTH-014 | role 值不合法 | 无 | `{"name":"Test","email":"y@university.edu","password":"Secure123!","role":"superadmin"}` | 400 | `{"error":"角色值不合法"}` | |

### 2.3 POST `/api/auth/logout` — 退出登录

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| AUTH-015 | 正常退出 | 用户已登录（Session 有效） | 无 | 200 | `{"message":"退出成功"}`，Session 被销毁 | |
| AUTH-016 | 未登录状态退出 | 无有效 Session | 无 | 200 | `{"message":"退出成功"}`（幂等操作，不报错） | |

### 2.4 GET `/api/auth/me` — 获取当前用户信息

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| AUTH-017 | 正常获取（已登录） | 用户 u001 已登录 | 无 | 200 | 返回 `{id, name, email, role, department, avatar}`，不包含 password | |
| AUTH-018 | 未登录访问 | 无有效 Session | 无 | 401 | `{"error":"未登录"}` | |
| AUTH-019 | Session 过期后访问 | Session 已超时 | 无 | 401 | `{"error":"未登录"}` | |
| AUTH-020 | 返回数据不包含密码 | 用户 u001 已登录 | 无 | 200 | 响应 JSON 中无 password 字段 | |

---

## 三、Applicant API 测试（10 个接口，45 个用例）

> 以下所有测试默认前置条件：用户 u001（applicant 角色）已通过 `/api/auth/login` 登录。

### 3.1 GET `/api/applicant/dashboard` — 获取仪表盘汇总

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-001 | 正常获取仪表盘数据 | u001 已登录，有申请 a001 | 无 | 200 | 返回 `{activeApplications, interviewCount, profileCompletion, recentApplications[], workload{current,max}, upcomingDeadlines[]}` | |
| APP-002 | 无申请的新用户仪表盘 | u002 已登录，无任何申请 | 无 | 200 | `activeApplications: 0, interviewCount: 0, recentApplications: []` | |
| APP-003 | 未登录访问 | 无有效 Session | 无 | 401 | `{"error":"未登录"}` | |
| APP-004 | 非 applicant 角色访问 | u005（organiser）已登录 | 无 | 403 | `{"error":"无权限访问"}` | |

### 3.2 GET `/api/applicant/jobs` — 获取职位列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-005 | 获取全部职位列表 | 已登录 | 无 | 200 | 返回 jobs 数组，只包含 status 为 active 的岗位 | |
| APP-006 | 关键词搜索 | 已登录 | `?keyword=Data` | 200 | 返回 title 或 description 中包含 "Data" 的岗位 | |
| APP-007 | 院系筛选 | 已登录 | `?department=Computer Science` | 200 | 返回 department 为 "Computer Science" 的岗位 | |
| APP-008 | 类型筛选 | 已登录 | `?type=Part-time` | 200 | 返回 type 为 "Part-time" 的岗位 | |
| APP-009 | 组合搜索与筛选 | 已登录 | `?keyword=Teaching&department=Computer Science&type=Part-time` | 200 | 同时满足三个条件的岗位 | |
| APP-010 | 搜索无结果 | 已登录 | `?keyword=NonExistentJob12345` | 200 | 返回空数组 `[]` | |
| APP-011 | 未登录访问 | 无有效 Session | 无 | 401 | `{"error":"未登录"}` | |

### 3.3 GET `/api/applicant/jobs/{id}` — 获取职位详情

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-012 | 正常获取职位详情 | 已登录，j001 存在 | `/api/applicant/jobs/j001` | 200 | 返回完整职位信息：`{id, moduleCode, title, description, professor, department, type, hoursPerWeek, positions, deadline, tags[], requirements[], responsibilities[]}` | |
| APP-013 | 职位 ID 不存在 | 已登录 | `/api/applicant/jobs/j999` | 404 | `{"error":"职位不存在"}` | |
| APP-014 | 获取已关闭的职位详情 | 已登录，j002 为 closed | `/api/applicant/jobs/j002` | 200 | 返回职位信息，status 为 closed（可查看但不可申请） | |

### 3.4 POST `/api/applicant/jobs/{id}/apply` — 提交申请

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| APP-015 | 正常提交申请 | u002 已登录，j001 为 active，u002 未申请过 j001 | 路径：`/api/applicant/jobs/j001/apply`，Body：`{"coverLetter":"Dear Committee...","documentIds":["doc002"]}` | 201 | 返回新申请信息（含 id、status: "applied"、timeline 初始化），data.json 中 applications 新增一条 | |
| APP-016 | 重复申请同一岗位 | u001 已登录，u001 已申请 j001 | 路径：`/api/applicant/jobs/j001/apply`，Body：`{"coverLetter":"...","documentIds":[]}` | 409 | `{"error":"已申请过该岗位"}` | |
| APP-017 | 向已关闭岗位申请 | 已登录，j002 status 为 closed | 路径：`/api/applicant/jobs/j002/apply`，Body：`{"coverLetter":"...","documentIds":[]}` | 400 | `{"error":"该岗位已关闭，无法申请"}` | |
| APP-018 | 缺少 coverLetter | 已登录 | 路径：`/api/applicant/jobs/j003/apply`，Body：`{"documentIds":[]}` | 400 | `{"error":"缺少必填字段"}` | |
| APP-019 | 岗位 ID 不存在 | 已登录 | 路径：`/api/applicant/jobs/j999/apply`，Body：`{"coverLetter":"...","documentIds":[]}` | 404 | `{"error":"职位不存在"}` | |

### 3.5 GET `/api/applicant/applications` — 获取申请列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-020 | 正常获取申请列表 | u001 已登录，有申请 a001 | 无 | 200 | 返回申请数组，每条包含 `{id, jobId, status, appliedAt, timeline[]}`，仅返回当前用户的申请 | |
| APP-021 | 无申请记录 | u002 已登录，无申请 | 无 | 200 | 返回空数组 `[]` | |
| APP-022 | 验证 timeline 数据完整性 | u001 已登录 | 无 | 200 | a001 的 timeline 包含 5 个状态节点（applied, review, shortlisted, interview, offer），各 completed/pending 字段正确 | |
| APP-023 | 不返回其他用户的申请 | u001 已登录 | 无 | 200 | 不包含其他用户（如 u002）的申请数据 | |

### 3.6 GET `/api/applicant/applications/{id}` — 获取申请详情

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-024 | 正常获取申请详情 | u001 已登录，a001 属于 u001 | `/api/applicant/applications/a001` | 200 | 返回完整申请信息：`{id, jobId, status, coverLetter, matchScore, aiSummary, appliedAt, timeline[]}` | |
| APP-025 | 申请 ID 不存在 | 已登录 | `/api/applicant/applications/a999` | 404 | `{"error":"申请不存在"}` | |
| APP-026 | 访问其他用户的申请 | u002 已登录，a001 属于 u001 | `/api/applicant/applications/a001` | 403 | `{"error":"无权限访问该申请"}` | |

### 3.7 GET `/api/applicant/profile` — 获取个人资料

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-027 | 正常获取个人资料 | u001 已登录，profile p001 存在 | 无 | 200 | 返回 `{id, userId, studentId, degree, year, gpa, maxGpa, profileCompletion, coursework[], experience[], documents[], maxWeeklyHours, currentWeeklyHours}` | |
| APP-028 | 新用户无档案 | 新注册用户已登录，尚未创建 profile | 无 | 200 | 返回空档案或默认值（profileCompletion: 0） | |

### 3.8 PUT `/api/applicant/profile` — 更新个人资料

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| APP-029 | 正常更新个人信息 | u001 已登录 | `{"degree":"MSc Computer Science","year":1,"gpa":3.9,"maxGpa":4.0}` | 200 | 返回更新后的 profile，data.json 中对应记录已更新 | |
| APP-030 | 更新 coursework 列表 | u001 已登录 | `{"coursework":[{"code":"CS301","name":"Database","grade":"A","term":"Fall 2026"}]}` | 200 | coursework 列表更新成功 | |
| APP-031 | 更新 experience 列表 | u001 已登录 | `{"experience":[{"role":"Lab Assistant","course":"CS201","term":"Fall 2026"}]}` | 200 | experience 列表更新成功 | |
| APP-032 | GPA 超出范围 | u001 已登录 | `{"gpa":5.0,"maxGpa":4.0}` | 400 | `{"error":"GPA 不能超过最大值"}` | |
| APP-033 | 未登录更新 | 无有效 Session | `{"degree":"test"}` | 401 | `{"error":"未登录"}` | |

### 3.9 POST `/api/applicant/profile/documents` — 上传文档

| 编号 | 描述 | 前置条件 | 请求 Body（multipart/form-data） | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------------------------|-----------|----------|----------|
| APP-034 | 正常上传 CV | u001 已登录 | file: CV.pdf, type: "cv" | 201 | 返回文档信息 `{id, name, type, size, uploadedAt}`，profile.documents 新增一条 | |
| APP-035 | 正常上传 Transcript | u001 已登录 | file: Transcript.pdf, type: "transcript" | 201 | 返回文档信息，type 为 "transcript" | |
| APP-036 | 不支持的文件类型 | u001 已登录 | file: image.exe, type: "cv" | 400 | `{"error":"不支持的文件类型"}` | |
| APP-037 | 缺少文件 | u001 已登录 | 无 file 字段 | 400 | `{"error":"缺少上传文件"}` | |

### 3.10 DELETE `/api/applicant/profile/documents/{id}` — 删除文档

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| APP-038 | 正常删除文档 | u001 已登录，doc001 属于 u001 | `/api/applicant/profile/documents/doc001` | 200 | `{"message":"文档删除成功"}`，profile.documents 中移除 doc001 | |
| APP-039 | 文档 ID 不存在 | u001 已登录 | `/api/applicant/profile/documents/doc999` | 404 | `{"error":"文档不存在"}` | |
| APP-040 | 删除其他用户的文档 | u002 已登录，doc001 属于 u001 | `/api/applicant/profile/documents/doc001` | 403 | `{"error":"无权限删除该文档"}` | |

### 3.11 Applicant 通用权限测试

| 编号 | 描述 | 前置条件 | 请求 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|------|-----------|----------|----------|
| APP-041 | organiser 角色访问 applicant 接口 | u005（organiser）已登录 | GET `/api/applicant/dashboard` | 403 | `{"error":"无权限访问"}` | |
| APP-042 | admin 角色访问 applicant 接口 | u010（admin）已登录 | GET `/api/applicant/jobs` | 403 | `{"error":"无权限访问"}` | |
| APP-043 | 未登录访问 applicant 接口 | 无 Session | GET `/api/applicant/profile` | 401 | `{"error":"未登录"}` | |
| APP-044 | 未登录提交申请 | 无 Session | POST `/api/applicant/jobs/j001/apply` | 401 | `{"error":"未登录"}` | |
| APP-045 | 未登录上传文档 | 无 Session | POST `/api/applicant/profile/documents` | 401 | `{"error":"未登录"}` | |

---

## 四、Organiser API 测试（12 个接口，55 个用例）

> 以下所有测试默认前置条件：用户 u005（organiser 角色）已通过 `/api/auth/login` 登录。

### 4.1 GET `/api/organiser/dashboard` — 获取组织者仪表盘

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ORG-001 | 正常获取仪表盘数据 | u005 已登录，管理 j001、j002 | 无 | 200 | 返回 `{activePostings, totalApplicants, pendingInterviews, recentActivities[], activeJobs[]}` | |
| ORG-002 | 新组织者无数据 | 新 organiser 已登录，无岗位 | 无 | 200 | `activePostings: 0, totalApplicants: 0, pendingInterviews: 0` | |
| ORG-003 | 未登录访问 | 无 Session | 无 | 401 | `{"error":"未登录"}` | |
| ORG-004 | applicant 角色访问 | u001（applicant）已登录 | 无 | 403 | `{"error":"无权限访问"}` | |

### 4.2 GET `/api/organiser/jobs` — 获取组织者管理的岗位列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ORG-005 | 正常获取岗位列表 | u005 已登录 | 无 | 200 | 返回 organiserId 为 u005 的所有 jobs（j001, j002），不含 u006 的岗位 | |
| ORG-006 | 仅返回当前用户的岗位 | u006 已登录 | 无 | 200 | 只返回 organiserId 为 u006 的岗位（j003） | |

### 4.3 POST `/api/organiser/jobs` — 创建新岗位

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| ORG-007 | 正常创建岗位 | u005 已登录 | `{"moduleId":"m001","moduleCode":"CS201","title":"TA - CS201 Lab","description":"...","type":"Part-time","hoursPerWeek":8,"positions":2,"deadline":"2026-11-15","tags":["Java"],"requirements":["A- in CS201"],"responsibilities":["Grade assignments"]}` | 201 | 返回新岗位（含自动生成 id、organiserId: u005、status: active），data.json 中 jobs 新增 | |
| ORG-008 | 缺少 title 字段 | u005 已登录 | `{"moduleId":"m001","type":"Part-time","hoursPerWeek":8}` | 400 | `{"error":"缺少必填字段"}` | |
| ORG-009 | 缺少 moduleId 字段 | u005 已登录 | `{"title":"TA Test","type":"Part-time","hoursPerWeek":8}` | 400 | `{"error":"缺少必填字段"}` | |
| ORG-010 | deadline 已过期 | u005 已登录 | `{"moduleId":"m001","title":"Old TA","deadline":"2020-01-01",...}` | 400 | `{"error":"截止日期不能为过去时间"}` | |

### 4.4 PUT `/api/organiser/jobs/{id}` — 编辑岗位

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ORG-011 | 正常编辑岗位 | u005 已登录，j001 属于 u005 | 路径：`/api/organiser/jobs/j001`，Body：`{"title":"Updated TA Position","positions":5}` | 200 | 返回更新后的 job，data.json 已更新 | |
| ORG-012 | 编辑其他组织者的岗位 | u005 已登录，j003 属于 u006 | 路径：`/api/organiser/jobs/j003`，Body：`{"title":"Hacked"}` | 403 | `{"error":"无权限编辑该岗位"}` | |
| ORG-013 | 岗位 ID 不存在 | u005 已登录 | 路径：`/api/organiser/jobs/j999`，Body：`{"title":"Test"}` | 404 | `{"error":"岗位不存在"}` | |

### 4.5 DELETE `/api/organiser/jobs/{id}` — 删除岗位

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ORG-014 | 正常删除岗位 | u005 已登录，j002 属于 u005 | `/api/organiser/jobs/j002` | 200 | `{"message":"岗位删除成功"}`，data.json 中移除 j002 | |
| ORG-015 | 删除其他组织者的岗位 | u005 已登录，j003 属于 u006 | `/api/organiser/jobs/j003` | 403 | `{"error":"无权限删除该岗位"}` | |
| ORG-016 | 删除不存在的岗位 | u005 已登录 | `/api/organiser/jobs/j999` | 404 | `{"error":"岗位不存在"}` | |

### 4.6 GET `/api/organiser/pipeline` — 获取招聘管道看板

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ORG-017 | 正常获取看板数据 | u005 已登录，有申请数据 | 无 | 200 | 返回按状态分组的候选人：`{applied[], review[], shortlisted[], interview[], offer[]}`，仅包含 u005 管理的岗位的申请 | |
| ORG-018 | 按 module 筛选 | u005 已登录 | `?module=CS201` | 200 | 仅返回 CS201 模块相关的候选人 | |
| ORG-019 | 筛选无结果的 module | u005 已登录 | `?module=NONEXIST` | 200 | 所有状态组均为空数组 | |
| ORG-020 | 不包含其他组织者的数据 | u005 已登录 | 无 | 200 | 不包含 u006 管理的岗位的申请者 | |

### 4.7 PUT `/api/organiser/pipeline/{candidateId}/status` — 更新候选人状态

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ORG-021 | 正常更新状态（applied -> review） | u005 已登录，a001 在 u005 管理范围 | 路径：`/api/organiser/pipeline/a001/status`，Body：`{"status":"review"}` | 200 | 返回更新后的申请，status 为 review，timeline 对应节点更新 | |
| ORG-022 | 正常更新状态（review -> shortlisted） | 同上 | Body：`{"status":"shortlisted"}` | 200 | status 更新为 shortlisted | |
| ORG-023 | 正常更新状态（shortlisted -> interview） | 同上 | Body：`{"status":"interview"}` | 200 | status 更新为 interview | |
| ORG-024 | 正常更新状态（interview -> offer） | 同上 | Body：`{"status":"offer"}` | 200 | status 更新为 offer | |
| ORG-025 | 设置为 rejected | 同上 | Body：`{"status":"rejected"}` | 200 | status 更新为 rejected | |
| ORG-026 | 不合法的 status 值 | u005 已登录 | Body：`{"status":"invalid_status"}` | 400 | `{"error":"状态值不合法"}` | |
| ORG-027 | 更新不属于自己的候选人 | u006 已登录，a001 属于 u005 管理的岗位 | 路径：`/api/organiser/pipeline/a001/status` | 403 | `{"error":"无权限操作该候选人"}` | |

### 4.8 GET `/api/organiser/applications/{id}` — 获取申请详情

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ORG-028 | 正常获取申请详情 | u005 已登录，a001 属于 u005 的岗位 | `/api/organiser/applications/a001` | 200 | 返回完整信息：`{application{...}, applicantProfile{name,degree,gpa,coursework,experience}, coverLetter, documents[], matchScore, aiSummary}` | |
| ORG-029 | 查看其他组织者的申请 | u006 已登录，a001 属于 u005 的岗位 | `/api/organiser/applications/a001` | 403 | `{"error":"无权限查看该申请"}` | |
| ORG-030 | 申请 ID 不存在 | u005 已登录 | `/api/organiser/applications/a999` | 404 | `{"error":"申请不存在"}` | |

### 4.9 PUT `/api/organiser/applications/{id}/action` — 对申请执行操作

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ORG-031 | 执行 shortlist 操作 | u005 已登录，a001 属于 u005 的岗位 | 路径：`/api/organiser/applications/a001/action`，Body：`{"action":"shortlist"}` | 200 | 申请 status 更新为 shortlisted，timeline 对应更新 | |
| ORG-032 | 执行 reject 操作 | 同上 | Body：`{"action":"reject"}` | 200 | 申请 status 更新为 rejected | |
| ORG-033 | 执行 offer 操作 | 同上 | Body：`{"action":"offer"}` | 200 | 申请 status 更新为 offer | |
| ORG-034 | 不合法的 action | 同上 | Body：`{"action":"delete"}` | 400 | `{"error":"操作类型不合法，仅支持 shortlist/reject/offer"}` | |
| ORG-035 | 缺少 action 字段 | 同上 | Body：`{}` | 400 | `{"error":"缺少必填字段"}` | |

### 4.10 GET `/api/organiser/interviews` — 获取面试列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ORG-036 | 正常获取面试列表 | u005 已登录，有面试 i001 | 无 | 200 | 返回面试数组，仅包含 organiserId 为 u005 的面试，每条含 `{id, candidateName, moduleCode, date, startTime, endTime, type, status, outcome}` | |
| ORG-037 | 无面试数据 | u006 已登录，无面试 | 无 | 200 | 返回空数组 `[]` | |

### 4.11 POST `/api/organiser/interviews` — 创建面试

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| ORG-038 | 正常创建面试 | u005 已登录 | `{"applicationId":"a001","candidateId":"u001","candidateName":"Alex Johnson","moduleCode":"CS201","moduleName":"Data Structures","date":"2026-11-01","startTime":"14:00","endTime":"14:30","type":"video"}` | 201 | 返回新面试信息（含自动 id、status: scheduled、organiserId: u005），data.json 中 interviews 新增 | |
| ORG-039 | 缺少 applicationId | u005 已登录 | `{"candidateId":"u001","date":"2026-11-01","startTime":"14:00"}` | 400 | `{"error":"缺少必填字段"}` | |
| ORG-040 | 缺少日期时间 | u005 已登录 | `{"applicationId":"a001","candidateId":"u001"}` | 400 | `{"error":"缺少必填字段"}` | |
| ORG-041 | type 值不合法 | u005 已登录 | `{"applicationId":"a001","candidateId":"u001","date":"2026-11-01","startTime":"14:00","endTime":"14:30","type":"phone"}` | 400 | `{"error":"面试类型不合法，仅支持 video/in-person"}` | |

### 4.12 PUT `/api/organiser/interviews/{id}` — 更新面试

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ORG-042 | 正常改期 | u005 已登录，i001 属于 u005 | 路径：`/api/organiser/interviews/i001`，Body：`{"date":"2026-11-05","startTime":"15:00","endTime":"15:30"}` | 200 | 面试日期时间更新成功 | |
| ORG-043 | 记录面试结果（offer） | u005 已登录，i001 status 为 upcoming | 路径同上，Body：`{"status":"completed","outcome":"offer"}` | 200 | status 更新为 completed，outcome 为 offer | |
| ORG-044 | 记录面试结果（reject） | 同上 | Body：`{"status":"completed","outcome":"reject"}` | 200 | status 更新为 completed，outcome 为 reject | |
| ORG-045 | 取消面试 | 同上 | Body：`{"status":"cancelled"}` | 200 | status 更新为 cancelled | |
| ORG-046 | 更新其他组织者的面试 | u006 已登录，i001 属于 u005 | 路径：`/api/organiser/interviews/i001`，Body：`{"status":"cancelled"}` | 403 | `{"error":"无权限操作该面试"}` | |
| ORG-047 | 面试 ID 不存在 | u005 已登录 | 路径：`/api/organiser/interviews/i999` | 404 | `{"error":"面试不存在"}` | |

### 4.13 Organiser 通用权限测试

| 编号 | 描述 | 前置条件 | 请求 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|------|-----------|----------|----------|
| ORG-048 | applicant 角色访问 organiser 接口 | u001（applicant）已登录 | GET `/api/organiser/dashboard` | 403 | `{"error":"无权限访问"}` | |
| ORG-049 | admin 角色访问 organiser 接口 | u010（admin）已登录 | GET `/api/organiser/jobs` | 403 | `{"error":"无权限访问"}` | |
| ORG-050 | 未登录访问 organiser 接口 | 无 Session | GET `/api/organiser/pipeline` | 401 | `{"error":"未登录"}` | |
| ORG-051 | 未登录创建岗位 | 无 Session | POST `/api/organiser/jobs` | 401 | `{"error":"未登录"}` | |
| ORG-052 | 未登录创建面试 | 无 Session | POST `/api/organiser/interviews` | 401 | `{"error":"未登录"}` | |
| ORG-053 | 未登录更新候选人状态 | 无 Session | PUT `/api/organiser/pipeline/a001/status` | 401 | `{"error":"未登录"}` | |
| ORG-054 | 未登录执行申请操作 | 无 Session | PUT `/api/organiser/applications/a001/action` | 401 | `{"error":"未登录"}` | |
| ORG-055 | 未登录删除岗位 | 无 Session | DELETE `/api/organiser/jobs/j001` | 401 | `{"error":"未登录"}` | |

---

## 五、Admin API 测试（16 个接口，70 个用例）

> 以下所有测试默认前置条件：用户 u010（admin 角色）已通过 `/api/auth/login` 登录。

### 5.1 GET `/api/admin/dashboard` — 获取平台分析数据

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-001 | 正常获取平台数据 | u010 已登录 | 无 | 200 | 返回 `{totalApplicants, activePostings, acceptanceRate, trendData[], moduleStats[], systemAlerts[]}` | |
| ADM-002 | 未登录访问 | 无 Session | 无 | 401 | `{"error":"未登录"}` | |
| ADM-003 | applicant 角色访问 | u001（applicant）已登录 | 无 | 403 | `{"error":"无权限访问"}` | |
| ADM-004 | organiser 角色访问 | u005（organiser）已登录 | 无 | 403 | `{"error":"无权限访问"}` | |

### 5.2 GET `/api/admin/users` — 获取用户列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-005 | 获取全部用户列表 | u010 已登录 | 无 | 200 | 返回所有用户数组，每条包含 `{id, name, email, role, department, status, lastLogin, createdAt}`，不包含 password | |
| ADM-006 | 关键词搜索 | u010 已登录 | `?keyword=Alex` | 200 | 返回 name 或 email 中包含 "Alex" 的用户 | |
| ADM-007 | 角色筛选 | u010 已登录 | `?role=applicant` | 200 | 仅返回 role 为 applicant 的用户 | |
| ADM-008 | 状态筛选 | u010 已登录 | `?status=active` | 200 | 仅返回 status 为 active 的用户 | |
| ADM-009 | 组合筛选 | u010 已登录 | `?keyword=&role=organiser&status=active` | 200 | 返回同时满足条件的用户 | |
| ADM-010 | 搜索无结果 | u010 已登录 | `?keyword=ZZZNoMatch` | 200 | 返回空数组 `[]` | |

### 5.3 POST `/api/admin/users` — 创建/邀请用户

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| ADM-011 | 正常创建用户 | u010 已登录 | `{"name":"New Faculty","email":"faculty@university.edu","role":"organiser","department":"Mathematics"}` | 201 | 返回新用户信息（含自动 id、status: active、默认密码），data.json 中 users 新增 | |
| ADM-012 | 邮箱已被占用 | u010 已登录 | `{"name":"Dup","email":"alex.j@university.edu","role":"applicant"}` | 409 | `{"error":"邮箱已被注册"}` | |
| ADM-013 | 缺少 name 字段 | u010 已登录 | `{"email":"x@university.edu","role":"applicant"}` | 400 | `{"error":"缺少必填字段"}` | |
| ADM-014 | 缺少 email 字段 | u010 已登录 | `{"name":"Test","role":"applicant"}` | 400 | `{"error":"缺少必填字段"}` | |
| ADM-015 | role 值不合法 | u010 已登录 | `{"name":"Test","email":"t@university.edu","role":"superuser"}` | 400 | `{"error":"角色值不合法"}` | |

### 5.4 PUT `/api/admin/users/{id}` — 编辑用户

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ADM-016 | 正常编辑用户角色 | u010 已登录，u001 存在 | 路径：`/api/admin/users/u001`，Body：`{"role":"organiser"}` | 200 | u001 角色更新为 organiser | |
| ADM-017 | 正常编辑用户状态 | u010 已登录 | 路径：`/api/admin/users/u003`，Body：`{"status":"active"}` | 200 | u003 状态更新为 active | |
| ADM-018 | 挂起用户 | u010 已登录 | 路径：`/api/admin/users/u002`，Body：`{"status":"suspended"}` | 200 | u002 状态更新为 suspended | |
| ADM-019 | 用户 ID 不存在 | u010 已登录 | 路径：`/api/admin/users/u999`，Body：`{"status":"active"}` | 404 | `{"error":"用户不存在"}` | |
| ADM-020 | 编辑自己的账户 | u010 已登录 | 路径：`/api/admin/users/u010`，Body：`{"role":"applicant"}` | 400 | `{"error":"不能修改自己的角色"}` | |

### 5.5 DELETE `/api/admin/users/{id}` — 删除用户

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-021 | 正常删除用户 | u010 已登录，u002 存在 | `/api/admin/users/u002` | 200 | `{"message":"用户删除成功"}`，data.json 中移除 u002 | |
| ADM-022 | 删除不存在的用户 | u010 已登录 | `/api/admin/users/u999` | 404 | `{"error":"用户不存在"}` | |
| ADM-023 | 删除自己的账户 | u010 已登录 | `/api/admin/users/u010` | 400 | `{"error":"不能删除自己的账户"}` | |

### 5.6 GET `/api/admin/modules` — 获取模块列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-024 | 获取全部模块列表 | u010 已登录 | 无 | 200 | 返回所有模块数组，每条含 `{id, code, name, department, organiserId, organiserName, term, status}` | |
| ADM-025 | 搜索模块 | u010 已登录 | `?keyword=Data` | 200 | 返回 name 或 code 中包含 "Data" 的模块 | |
| ADM-026 | 搜索无结果 | u010 已登录 | `?keyword=NonExistModule` | 200 | 返回空数组 `[]` | |

### 5.7 POST `/api/admin/modules` — 新增模块

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| ADM-027 | 正常新增模块 | u010 已登录 | `{"code":"CS401","name":"Machine Learning","department":"Computer Science","organiserId":"u005","organiserName":"Dr. Alan Turing","term":"Fall 2026"}` | 201 | 返回新模块（含自动 id、status: active），data.json 中 modules 新增 | |
| ADM-028 | 缺少 code 字段 | u010 已登录 | `{"name":"ML","department":"CS"}` | 400 | `{"error":"缺少必填字段"}` | |
| ADM-029 | 缺少 name 字段 | u010 已登录 | `{"code":"CS401","department":"CS"}` | 400 | `{"error":"缺少必填字段"}` | |
| ADM-030 | 模块代码重复 | u010 已登录，CS201 已存在 | `{"code":"CS201","name":"Dup Module","department":"CS"}` | 409 | `{"error":"模块代码已存在"}` | |

### 5.8 PUT `/api/admin/modules/{id}` — 编辑模块

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ADM-031 | 正常编辑模块 | u010 已登录，m001 存在 | 路径：`/api/admin/modules/m001`，Body：`{"name":"Advanced Data Structures","status":"draft"}` | 200 | 返回更新后的模块 | |
| ADM-032 | 模块 ID 不存在 | u010 已登录 | 路径：`/api/admin/modules/m999`，Body：`{"name":"Test"}` | 404 | `{"error":"模块不存在"}` | |

### 5.9 DELETE `/api/admin/modules/{id}` — 删除模块

| 编号 | 描述 | 前置条件 | 请求路径 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-033 | 正常删除模块 | u010 已登录，m002 存在 | `/api/admin/modules/m002` | 200 | `{"message":"模块删除成功"}`，data.json 中移除 m002 | |
| ADM-034 | 删除不存在的模块 | u010 已登录 | `/api/admin/modules/m999` | 404 | `{"error":"模块不存在"}` | |
| ADM-035 | 删除有关联岗位的模块 | u010 已登录，m001 关联 j001 | `/api/admin/modules/m001` | 400 | `{"error":"该模块下存在关联岗位，无法直接删除"}` | |

### 5.10 GET `/api/admin/properties/terms` — 获取学期列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-036 | 正常获取学期列表 | u010 已登录 | 无 | 200 | 返回学期数组，每条含 `{id, name, startDate, endDate, status}` | |

### 5.11 POST `/api/admin/properties/terms` — 新增学期

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| ADM-037 | 正常新增学期 | u010 已登录 | `{"name":"Spring 2027","startDate":"2027-01-15","endDate":"2027-05-20"}` | 201 | 返回新学期（含自动 id、status: active），data.json 中 terms 新增 | |
| ADM-038 | 缺少 name 字段 | u010 已登录 | `{"startDate":"2027-01-15","endDate":"2027-05-20"}` | 400 | `{"error":"缺少必填字段"}` | |
| ADM-039 | 开始日期晚于结束日期 | u010 已登录 | `{"name":"Invalid","startDate":"2027-12-01","endDate":"2027-01-01"}` | 400 | `{"error":"开始日期不能晚于结束日期"}` | |
| ADM-040 | 学期名称重复 | u010 已登录，"Fall 2026" 已存在 | `{"name":"Fall 2026","startDate":"2026-08-15","endDate":"2026-12-20"}` | 409 | `{"error":"学期名称已存在"}` | |

### 5.12 GET `/api/admin/properties/departments` — 获取院系列表

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-041 | 正常获取院系列表 | u010 已登录 | 无 | 200 | 返回院系数组，每条含 `{id, name, moduleCount, status}` | |

### 5.13 POST `/api/admin/properties/departments` — 新增院系

| 编号 | 描述 | 前置条件 | 请求 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|-----------|-----------|----------|----------|
| ADM-042 | 正常新增院系 | u010 已登录 | `{"name":"Mathematics"}` | 201 | 返回新院系（含自动 id、moduleCount: 0、status: active） | |
| ADM-043 | 缺少 name 字段 | u010 已登录 | `{}` | 400 | `{"error":"缺少必填字段"}` | |
| ADM-044 | 院系名称重复 | u010 已登录，"Computer Science" 已存在 | `{"name":"Computer Science"}` | 409 | `{"error":"院系名称已存在"}` | |

### 5.14 GET `/api/admin/logs` — 获取系统日志

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-045 | 获取全部日志 | u010 已登录 | 无 | 200 | 返回日志数组，按时间倒序排列，每条含 `{id, timestamp, action, actor, actorId, type, details, ip}` | |
| ADM-046 | 关键词搜索日志 | u010 已登录 | `?keyword=Login` | 200 | 返回 action 或 details 中包含 "Login" 的日志 | |
| ADM-047 | 类型筛选日志 | u010 已登录 | `?type=error` | 200 | 仅返回 type 为 error 的日志 | |
| ADM-048 | 组合搜索筛选 | u010 已登录 | `?keyword=auth&type=info` | 200 | 同时满足关键词和类型条件 | |
| ADM-049 | 搜索无结果 | u010 已登录 | `?keyword=ZZZZZ` | 200 | 返回空数组 `[]` | |

### 5.15 GET `/api/admin/workload` — 获取工作量分析

| 编号 | 描述 | 前置条件 | 请求参数 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------|-----------|----------|----------|
| ADM-050 | 获取全部工作量数据 | u010 已登录 | 无 | 200 | 返回 TA 工作量数组，包含 `{taUserId, taName, assignments[], totalHours, maxHours, status}` | |
| ADM-051 | 关键词搜索 | u010 已登录 | `?keyword=Alex` | 200 | 返回名称中包含 "Alex" 的 TA 工作量 | |
| ADM-052 | 状态筛选（超负荷） | u010 已登录 | `?status=overloaded` | 200 | 仅返回工作时间超过 maxHours 的 TA | |
| ADM-053 | 搜索无结果 | u010 已登录 | `?keyword=NonExistTA` | 200 | 返回空数组 `[]` | |

### 5.16 PUT `/api/admin/workload/{taId}/reassign` — 重新分配工作量

| 编号 | 描述 | 前置条件 | 请求路径与 Body | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|----------------|-----------|----------|----------|
| ADM-054 | 正常重新分配 | u010 已登录，u001 有工作量分配 | 路径：`/api/admin/workload/u001/reassign`，Body：`{"removeModules":["CS201"],"addModules":[{"moduleCode":"CS301","hoursPerWeek":6}]}` | 200 | 工作量分配更新成功，u001 的 workloadAssignments 已变更 | |
| ADM-055 | TA 用户不存在 | u010 已登录 | 路径：`/api/admin/workload/u999/reassign`，Body：`{"removeModules":["CS201"]}` | 404 | `{"error":"TA 用户不存在"}` | |
| ADM-056 | 分配后超出最大工时 | u010 已登录 | Body 中 addModules 累加后超过 maxWeeklyHours | 400 | `{"error":"分配后总工时超过最大工时限制"}` | |

### 5.17 Admin 通用权限测试

| 编号 | 描述 | 前置条件 | 请求 | 预期状态码 | 预期响应 | 测试结果 |
|------|------|----------|------|-----------|----------|----------|
| ADM-057 | applicant 访问 admin 接口 | u001（applicant）已登录 | GET `/api/admin/dashboard` | 403 | `{"error":"无权限访问"}` | |
| ADM-058 | organiser 访问 admin 接口 | u005（organiser）已登录 | GET `/api/admin/users` | 403 | `{"error":"无权限访问"}` | |
| ADM-059 | 未登录访问 admin 接口 | 无 Session | GET `/api/admin/dashboard` | 401 | `{"error":"未登录"}` | |
| ADM-060 | 未登录创建用户 | 无 Session | POST `/api/admin/users` | 401 | `{"error":"未登录"}` | |
| ADM-061 | 未登录删除用户 | 无 Session | DELETE `/api/admin/users/u001` | 401 | `{"error":"未登录"}` | |
| ADM-062 | 未登录创建模块 | 无 Session | POST `/api/admin/modules` | 401 | `{"error":"未登录"}` | |
| ADM-063 | 未登录删除模块 | 无 Session | DELETE `/api/admin/modules/m001` | 401 | `{"error":"未登录"}` | |
| ADM-064 | 未登录新增学期 | 无 Session | POST `/api/admin/properties/terms` | 401 | `{"error":"未登录"}` | |
| ADM-065 | 未登录新增院系 | 无 Session | POST `/api/admin/properties/departments` | 401 | `{"error":"未登录"}` | |
| ADM-066 | 未登录查看日志 | 无 Session | GET `/api/admin/logs` | 401 | `{"error":"未登录"}` | |
| ADM-067 | 未登录查看工作量 | 无 Session | GET `/api/admin/workload` | 401 | `{"error":"未登录"}` | |
| ADM-068 | 未登录重分配工作量 | 无 Session | PUT `/api/admin/workload/u001/reassign` | 401 | `{"error":"未登录"}` | |
| ADM-069 | applicant 尝试删除用户 | u001（applicant）已登录 | DELETE `/api/admin/users/u002` | 403 | `{"error":"无权限访问"}` | |
| ADM-070 | organiser 尝试创建模块 | u005（organiser）已登录 | POST `/api/admin/modules` | 403 | `{"error":"无权限访问"}` | |

---

## 六、跨模块集成测试（12 个用例）

### 6.1 完整申请流程（端到端）

| 编号 | 描述 | 步骤 | 预期结果 | 测试结果 |
|------|------|------|----------|----------|
| INT-001 | 完整申请到 Offer 流程 | 1. Organiser 登录 -> 创建岗位<br>2. Applicant 注册 -> 登录 -> 浏览岗位 -> 提交申请<br>3. Organiser 查看 pipeline -> 移动状态到 review -> shortlisted -> interview<br>4. Organiser 创建面试 -> 完成面试 -> 发出 offer<br>5. Applicant 查看 tracker 确认 offer | 全流程各步骤状态正确，timeline 完整记录，数据一致 | |
| INT-002 | 完整申请到 Reject 流程 | 1. Applicant 提交申请<br>2. Organiser 在 pipeline 中 reject 候选人 | 申请 status 变为 rejected，Applicant tracker 中可见 | |
| INT-003 | 多角色数据隔离 | 1. Applicant A 和 B 各自提交申请<br>2. A 查看申请列表<br>3. B 查看申请列表 | A 只看到自己的申请，B 只看到自己的申请，数据互不干扰 | |

### 6.2 数据一致性测试

| 编号 | 描述 | 步骤 | 预期结果 | 测试结果 |
|------|------|------|----------|----------|
| INT-004 | 申请后 Dashboard 数据更新 | 1. Applicant 查看 dashboard（记录初始值）<br>2. 提交一个新申请<br>3. 再次查看 dashboard | activeApplications 增加 1，recentApplications 包含新申请 | |
| INT-005 | 删除岗位后关联申请处理 | 1. Organiser 删除一个有申请的岗位<br>2. Applicant 查看 applications | 相关申请状态应正确处理（显示岗位已关闭或已删除标记） | |
| INT-006 | 删除用户后关联数据处理 | 1. Admin 删除一个 applicant 用户<br>2. Organiser 查看 pipeline | 已删除用户的申请不再显示或标记为已删除 | |
| INT-007 | 工作量分配一致性 | 1. Admin 查看 u001 工作量<br>2. Organiser 给 u001 发 offer（新增一个 TA 岗位）<br>3. Admin 再次查看 u001 工作量 | 工作量数据反映最新分配，totalHours 正确累加 | |

### 6.3 并发与幂等性测试

| 编号 | 描述 | 步骤 | 预期结果 | 测试结果 |
|------|------|------|----------|----------|
| INT-008 | 重复提交申请 | 同一 Applicant 对同一岗位连续发送两次 apply 请求 | 第一次成功（201），第二次失败（409），数据库中只有一条申请 | |
| INT-009 | 并发更新候选人状态 | 两个请求同时将同一候选人从 review 移动到 shortlisted 和 rejected | 只有一个操作成功，不出现状态冲突 | |
| INT-010 | 登出后使用旧 Session | 1. 登录获取 Session<br>2. 登出<br>3. 使用旧 Session 调用接口 | 步骤 3 返回 401，Session 已失效 | |

### 6.4 注册与 Profile 关联

| 编号 | 描述 | 步骤 | 预期结果 | 测试结果 |
|------|------|------|----------|----------|
| INT-011 | 新注册用户获取 Profile | 1. 通过 signup 注册新用户<br>2. 登录<br>3. GET /api/applicant/profile | 返回默认空 profile 或自动创建的 profile（profileCompletion: 0） | |
| INT-012 | Admin 创建用户后该用户可登录 | 1. Admin 通过 POST /api/admin/users 创建用户<br>2. 新用户使用创建时的凭据登录 | 登录成功，返回正确的用户信息和角色 | |

---

## 七、非功能测试（5 个用例）

### 7.1 Filter 与中间件测试

| 编号 | 描述 | 步骤 | 预期结果 | 测试结果 |
|------|------|------|----------|----------|
| NF-001 | CORS 头验证 | 发送 OPTIONS 预检请求到任意 API | 响应包含 `Access-Control-Allow-Origin`、`Access-Control-Allow-Methods`、`Access-Control-Allow-Headers` 头 | |
| NF-002 | AuthFilter 拦截未登录请求 | 无 Session 状态下访问 `/api/applicant/dashboard` | 被 AuthFilter 拦截，返回 401，不进入 Servlet 处理逻辑 | |
| NF-003 | AuthFilter 放行公共接口 | 无 Session 状态下访问 `/api/auth/login` | 请求正常到达 AuthServlet，不被拦截 | |

### 7.2 数据格式与错误处理

| 编号 | 描述 | 步骤 | 预期结果 | 测试结果 |
|------|------|------|----------|----------|
| NF-004 | 统一 JSON 响应格式 | 调用任意接口 | Content-Type 为 `application/json;charset=UTF-8`，响应体为合法 JSON | |
| NF-005 | 统一错误响应格式 | 触发各类错误（400/401/403/404/409） | 错误响应统一格式为 `{"error":"错误描述信息"}`，不泄露堆栈信息或内部实现细节 | |

---

## 八、测试用例统计汇总

| 模块 | 接口数 | 用例数 | 覆盖维度 |
|------|--------|--------|----------|
| Auth（公共认证） | 4 | 20 | 正常流程 / 参数校验 / 业务异常 / Session 管理 |
| Applicant（申请者） | 10 | 45 | 正常流程 / 搜索筛选 / 权限控制 / 数据隔离 / 文档管理 |
| Organiser（组织者） | 12 | 55 | 正常流程 / 状态流转 / 权限控制 / 数据归属 / 面试管理 |
| Admin（管理员） | 16 | 70 | 正常流程 / CRUD 全覆盖 / 权限控制 / 级联影响 / 工作量分析 |
| 集成测试 | — | 12 | 端到端流程 / 数据一致性 / 并发幂等 / 关联数据 |
| 非功能测试 | — | 5 | CORS / 认证拦截 / 响应格式 / 错误处理 |
| **总计** | **42** | **207** | **100% 接口覆盖** |

---

## 九、附录：HTTP 状态码参考

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | OK | 查询成功、更新成功、删除成功 |
| 201 | Created | 创建资源成功（注册、新增岗位、提交申请等） |
| 400 | Bad Request | 缺少必填字段、参数格式不正确、业务校验失败 |
| 401 | Unauthorized | 未登录（无有效 Session） |
| 403 | Forbidden | 角色不匹配、无权限访问、账户被挂起 |
| 404 | Not Found | 资源不存在（用户/岗位/申请/面试/模块等） |
| 409 | Conflict | 唯一性冲突（邮箱重复、重复申请、名称重复等） |
| 500 | Internal Server Error | 服务器内部错误（不应主动返回，作为兜底） |

---

## 十、附录：枚举值参考

| 字段 | 所属集合 | 合法枚举值 |
|------|----------|-----------|
| role | users | `applicant` / `organiser` / `admin` |
| status | users | `active` / `suspended` / `inactive` |
| status | modules | `active` / `draft` / `inactive` |
| status | jobs | `active` / `draft` / `closed` |
| status | applications | `applied` / `review` / `shortlisted` / `interview` / `offer` / `rejected` |
| riskFlag | applications | `null` / `overload` / `missing_req` |
| type | interviews | `video` / `in-person` |
| status | interviews | `scheduled` / `upcoming` / `completed` / `cancelled` |
| outcome | interviews | `null` / `offer` / `reject` |
| type | logs | `info` / `success` / `warning` / `error` |
