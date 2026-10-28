# XinyiXu 本次改动记录

更新日期：2026-10-28。工作分支：`XinyiXu`，基线：`d8f1835`（Version 1）。

状态：用户已确认提交与推送本记录涵盖的累计改动。本记录汇总 Interview 移除、用户角色检查中的第 1–5 项修复，以及后续确认的 Teacher 直选、admin 专属 CSV 和角色名称统一。此前积累的前端改动仍保留在同一工作区，见文末。

## 1. 移除 Interview 功能

- 删除教师 Interviews 页面、导航、排期/编辑操作及对应 GET/POST/PUT/DELETE API；原 API 返回 404。
- 删除申请流程、详情、仪表盘中的面试按钮、阶段、统计和事件；隐藏历史面试通知。
- Pipeline 使用 Applied → Review → Shortlisted → Offer / Rejected。正式录用审批继续在 Review Workspace 完成；Section 分配与学生回复继续使用 TA Assignments / My Assignments。
- Data Editor 不再提供 interviews 集合，直接访问返回 410；禁止新增 interview 状态。
- 保留历史数据、模型和数据库映射。旧 interview 状态在活动界面投影为 review，不批量改写历史记录。
- 更新首页介绍及中英文 README、使用手册。旧 PDF、截图和生成的 Javadoc 未重新生成。

详细兼容说明见 [interview-retirement.md](interview-retirement.md)。

## 2. 修复角色检查第 1–5 项

| 原问题 | 本次行为 | 主要代码 |
| --- | --- | --- |
| 1. 消息资料接口可查询无关学生资料 | 查询前检查请求人、目标账号状态及授权关系；无权返回 403，目标不存在返回 404。教师仅看到当前负责且有审核权限的课程对应的学生申请摘要；学生间不能借该入口读取彼此资料。联系人、聊天记录和未读计数也遵守权限边界。 | MessageService、MessageServlet |
| 2. 撤销审核权限后仍可通过旧 Pipeline 操作 | Dashboard、Pipeline、申请详情及关联分配候选入口检查 review.read。Shortlist 等操作要求 recommend 或 approve；正式拒绝要求 approve。接口拒绝未授权调用，页面按服务端能力标记显示按钮，缺少标记时默认不显示操作。 | ReviewAccess、OrganiserService / Servlet、SectionAssignmentService、教师页面 |
| 3. 课程转交后新旧教师职责错位 | 以当前 Module 所属教师为课程访问依据。管理员转交课程时先验证接收人为有效教师，再同步该课程所有职位的负责人及姓名，包括已关闭职位。新教师可维护职位、条件、审核和分配；旧教师失去当前课程访问。正常课程编辑与 Data Editor 课程编辑使用同一逻辑。 | AdminService、ReviewAccess、CriteriaServlet、SectionMatchingService、SectionAssignmentService |
| 4. 旧 Reject 路径绕过正式审批、造成状态冲突 | Reject 必须有 review.approve 并填写 10–4000 字符理由，通过 ReviewService 记录 not_selected、操作者及依据。已有有效分配时先取消分配；已决策申请不能从旧 Pipeline 倒退到待审状态。Offer 仍要求已有正式批准。Data Editor 禁止改变已有申请状态，指向 Pipeline / Review Workspace；允许保持原状态编辑其他字段。 | OrganiserService / Servlet、ReviewService、AdminService、Pipeline / 详情页面 |
| 5. Data Editor users 可绕过账号业务规则 | 删除 users 编辑入口；通用 users 新增/更新/删除在服务及 HTTP 层返回 403，提示使用 Users。保留其他集合必要的安全用户引用，不返回密码字段。Users 业务接口验证角色及状态，禁止管理员禁用本人，并保留本人角色修改/删除保护。 | AdminService、AdminServlet、admin/data-editor.jsp |

### 课程交接与历史记录

课程和职位的当前责任人会变更；既有申请内容、审核记录、分配记录、历史操作者及快照不因交接重写。匹配与新操作使用当前课程责任人。即使旧数据的 Job 负责人字段不同步，也不能作为旧教师继续访问的依据。

### 页面与接口配合

- Pipeline 和申请详情接收 canApprove、canRecommend、canNote、selectionApproved 等能力标记。
- 无批准权限时隐藏 Reject；有权限时要求输入理由，取消输入不发送请求。
- 前端隐藏按钮之外，后端仍独立校验权限、课程范围及状态前置条件。

## 3. 验证记录

上一轮结果：377 项 Java 测试通过，失败 / 错误 / 跳过均为 0（明确排除 PostgresStoreTest）；24 项前端测试通过，失败 / 跳过均为 0。Data Editor 内联 JavaScript 语法检查及 git diff --check 通过。

复核命令：

```sh
mvn -o -f backend/pom.xml '-Dtest=*,!PostgresStoreTest' test
node --test tests/frontend/*.test.cjs
git diff --check
```

前端 Chromium 用例需设置 PLAYWRIGHT_MODULE 和 CHROMIUM_EXECUTABLE 指向已安装的 Playwright 模块与 Chromium。

验证覆盖无关用户资料查询、撤销权限、课程交接、新旧教师访问、审批理由与记录一致性、通用账号写入拦截、自账号保护、Interview 退役及前端交互。审查另外发现 Data Editor 仍能直接改申请状态，已先复现，再新增保护与回归测试。

测试主要使用合成内存数据；浏览器检查使用页面脚本和替换 JSP 指令后的页面体，并拦截合成 API 响应。它们不证明真实 JSP 编译、运行中的服务已更新或真实 PostgreSQL 数据保存成功。独立的 PostgresStoreTest 本次未运行；本次没有修改本地真实账号、课程归属或权限数据。

## 4. 同一工作区中保留的此前改动

- 登录页输入邮箱后进入 Create account，带入已填邮箱。
- 收紧 applicant 的页面和功能入口，包括此前的 CSV 访问限制。
- 学生 TA Application Profile 分区展示、校验与编辑交互；保存后为只读展示，点击 Edit 才进入编辑，可取消恢复。

这些是此前积累的改动，不表示本次重新实现；已连同本次修复一并获得用户提交授权。

## 5. Teacher 直选 TA（后续批准范围）

入口：Teacher 的申请详情页，以及 Review Workspace 中的 **Select as TA**。

1. applicant 正常提交课程职位申请及资格 / availability 快照。
2. 当前课程 Teacher 打开申请，选择职位涵盖的有效 Section，填写 10–4000 字符录用理由。availability 待确认时，需要勾选明确确认并填写 10–1000 字符说明；有明确时间冲突时不能分配。
3. 提交成功会记录带操作者、Section 与匹配证据的正式 approve 决策，把申请置为 offer，并建立 Section 分配草稿；此时尚不向 applicant 发布分配。
4. Teacher 通过链接进入 TA Assignments，检查草稿、填写回复截止时间并发布。
5. applicant 在 My Assignments 查看已发布分配并接受或拒绝；admin 可查看及管理所有课程的分配。

Teacher 的默认权限现在包含 review.approve；已显式配置 permissions 的账号保持原配置，因此被撤销的权限不会被新默认值覆盖。原有 reviewer / decision_maker / viewer 权限不因这个默认值变更而升级。

直选先在隔离数据副本上执行完整审批和分配校验，通过后再在当前请求事务和共享锁内应用。名额不足、重复分配、课程 / 周期 / 职位 Section 不符、排期冲突、无效账号或缺少权限时，不留下半条批准记录。既有正式决定不会被直选静默覆盖，重复请求返回冲突，不重复创建草稿。匹配继续作为人工判断依据。

## 6. CSV 与角色显示名称

- 完整 CSV Import & Export 仅供有效 admin 使用。删除 Teacher CSV 页面及侧栏菜单，并在 AuthFilter、CSV Servlet 和业务服务独立校验；Teacher 和高级审核角色直接调用导入、导出、模板或 options 接口也会被拒绝。
- 主角色显示名称统一为 `applicant`、`Teacher`、`admin`，覆盖登录、导航、角色徽章、账号管理、课程负责人及当前使用说明。使用 RoleLabels 统一转换。
- 保留内部 `organiser` 角色值、路径和数据库兼容性；未自动迁移或删除真实账号。既有 reviewer、decision_maker、viewer 明确显示为高级审核角色。
- 已更新 README、使用手册和 Sponsor CSV 指南。历史审查记录保留当时发现，不作为当前行为说明。

### 本轮验证

后端全量非 PostgreSQL 回归：408 项通过，失败 / 错误 / 跳过均为 0。其后补充成功 HTTP 路径与会话操作者测试，直选服务 / 接口定向复核 22 项通过（其中服务 19 项、接口 3 项）。前端 30 项全部通过，失败 / 跳过均为 0；20 段改动 JSP 内联脚本语法检查及 git diff --check 通过。

代码审查发现并修正两处界面问题：待确认 availability 的说明上限与后端统一为 1000 字符；已有分配使用中性状态提示，避免把已发布或已接受分配误称为草稿。新增回归先复现失败再修复，复核未发现剩余具体问题。

上述测试使用合成数据及页面夹具。本轮未运行独立 PostgresStoreTest，也未验证运行中的服务已部署、真实 JSP 编译或真实数据库保存。没有修改本地真实账号权限或课程数据。

## 7. 范围与提交确认

- 初轮完成角色检查第 1–5 项；本轮进一步实现第 6–8 项涉及的 CSV 管理收口和 Teacher 直选，并统一主角色名称。
- Review Workspace / Pipeline 合并、精细字段可见性及高级角色迁移不在此次实现范围。
- 用户已确认将上述累计改动提交并推送至 `XinyiXu`；最终提交哈希及远程状态以 Git 记录为准。
