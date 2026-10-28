# 用户角色与功能分级检查

日期：2026-10-28。检查当前工作区源码，包括尚未提交的修复。此次不修改业务代码、不修改本地真实账号数据。复现仅在 `JsonDataService.detached(TestDataFactory.createTestDataStore())` 创建的合成内存数据中执行；不是对正在运行服务的渗透测试，也不是完整安全审计。

> 修复进度：以下第 1–5 项的原始问题已完成代码修复及回归覆盖，待用户确认提交。当前行为及验证边界见 [XinyiXu-change-log.md](XinyiXu-change-log.md)。后续批准范围中已实现第 6–8 项涉及的 admin 专属 CSV 与 Teacher 直选，并统一三种主角色显示名称（第 12 项命名部分）；高级角色仍保留。下文保留审查时的原始发现，行号对应当时源码，其余建议未宣称全部解决。

## 一 已确认的实现问题

### 1 消息用户资料查询缺少对象访问校验 高优先级

`MessageServlet.java` 的 `user-info/{id}` 调用 `MessageService.getUserInfo(requesterId,targetUserId)`。`MessageService.java:551` 起的实现没有使用 requesterId 做权限判断，返回目标学生的邮箱、学术资料/GPA及全部申请的课程和状态。

合成复现：学生 A 对学生 B 的 `canMessage` 为 false，但 `getUserInfo` 仍返回 profile 与 applications。不应把“前端联系人列表看不到”当成权限保障。建议按本人、当前授权课程及明确用途限制返回对象和字段。

### 2 自定义审核权限只在部分入口生效 高优先级

`ReviewService.java:12` 检查 `review.read` 和当前课程范围；`OrganiserService.getPipeline/getApplicationDetails/updateCandidateStatus` 仍主要按 Job.organiserId 判断，没有统一检查自定义 review 权限。

合成复现：把教师 permissions 设为空，Review Workspace 返回 403，但旧 Pipeline 仍返回两条申请，shortlist 操作仍成功。撤销权限不应因入口不同而失效。

### 3 课程转交后 新旧教师的范围和职责错位 高优先级

- `AdminService.java:456` 更新 Module.organiserId，未同步现有 Job.organiserId，也没有定义旧职位交接策略。
- `OrganiserService.java:409/532/756` 等旧路径仍依据职位上的旧教师，旧教师可能继续编辑/删除旧职位或查看申请。
- 新课程教师能够通过使用 Module 归属的审核、Section、附件路径访问课程，但旧职位和匹配条件操作可能被拒绝。
- `CriteriaServlet.java:11` 同时要求职位归属和当前课程归属；两者不同会使新旧教师都无法维护条件。
- `SectionAssignmentService.java:146/231` 也会因归属不一致拒绝分配或漏掉已批准候选人。

合成复现确认课程归属改变后，旧教师仍可取得旧申请详情。应统一当前课程所有权，并明确是否同步职位、保留历史操作者和如何交接进行中的分配。

### 4 教师可通过旧拒绝操作绕过最终决定权限 并产生状态矛盾 高优先级

`ReviewService.record` 的 approve/not_selected 需要 `review.approve`；`OrganiserService.updateCandidateStatus` 的 rejected 不要求该权限，只检查职位所有权和有效分配等约束。它也不会写入对应的 not_selected 审核决定。

合成复现：默认教师没有 `review.approve`，但能把已批准、未分配的申请改为 rejected；此时学生申请状态是 rejected，Review Workspace 的 selectionStatus 仍为 selected。

建议把最终不选用和批准统一到审核决定服务；如果普通教师只能表达推荐意见，应区分其意见和正式决定。

### 5 Data Editor 的账号修改绕过常规 Users 规则 高优先级

`AdminService.updateUser` 有合法角色校验、禁止修改本人角色等规则；`updateCollectionItem("users",...)` 的通用修改路径未复用这些规则，能直接设置 role。`AdminServlet` 该通用路径也没有传入操作者用于本人账号保护。

合成复现：通过通用 Data Editor 可以把管理员角色改成不存在的 invented_role。它不是学生提权，但会破坏角色体系并可能让管理账号失去访问能力。建议关闭通用账号编辑，统一通过 Users 业务服务管理。

## 二 职责分配与交互设计的不一致 需要确认后调整

### 6 教师端仍开放整套 CSV Import & Export

`common/sidebar.jsp` 的 organiser 分支有 CSV 菜单，`organiser/csv.jsp` 页面存在；服务允许教师导入自己课程的数据、按课程导出。已有范围校验，因此不能称为当前必然能跨课程导入。按本次讨论的职责建议，应只在 Admin 保留批量管理；教师若需名册，提供独立课程导出按钮。

### 7 查看权限与批量导出权限没有分开

`SponsorCsvService.export` 主要使用 `review.read` 加课程范围，没有独立 export 权限；Reviewer/Decision-maker/Viewer 只要满足条件，也可直接调用共享 CSV API，即使菜单不显示 CSV。只读不必然禁止导出，但政策需要明确，尤其学生导出数据集包含学号及其他细节。建议单独定义导出权限和字段范围。

### 8 教师直选 TA 的产品目标尚未实现

教师默认有查看、备注和推荐权限，没有批准权限；管理员可授予批准，但“Select as TA → 记录决定并建立分配草稿”的统一操作尚未实现。当前限制不是单独的权限漏洞，而是与拟新增功能之间的缺口。应按授权教师、所属课程及现有分配校验实现，不应简单给所有教师完整管理员权限。

### 9 Review Workspace 与 Pipeline 重复管理申请

两套入口同时存在，使用不同的状态及动作路径，已经出现第 2、4 项不一致。建议明确 Review Workspace 是正式审核记录来源，Pipeline 仅作同一状态来源的视图，或合并入口。

### 10 Viewer 只有只读动作 但没有独立字段级范围

`ReviewService.row` 会把完整 qualificationSnapshot 放入返回值；没有按 Viewer/Reviewer/Teacher 分别裁剪学生 ID、DSC accommodation 等字段。当前有课程访问控制，并非公开访问，但不同角色是否都需要全部字段尚无明确策略。建议与 sponsor 确认字段最小集；不能仅因 Viewer 不可编辑就认定其看到的信息已经合适。

### 11 登录页要求用户自行选择角色 且有两套选择控件

`auth/login.jsp` 同时使用三角色单选和额外 review staff 下拉，都会改变 selectedRole。后端会核对真实角色，因此不是任意选择就能提权；问题是角色判断负担交给用户，且显示选择可能与内部状态不同步。建议登录后按账号真实角色导航；如将来允许多角色，登录后再选择授权工作区。

### 12 三类主要用户与六个内部角色的展示不一致

后端角色为 applicant、organiser、admin、reviewer、decision_maker、viewer；Properties 的静态 Role Designations 只说明三种。Teacher/Organiser/Module Organiser、Admin/Adviser、Applicant/TA 的叫法也不统一。六角色本身不一定多余：它们支持分离查看、推荐和最终决定。应统一名称与说明，明确额外审核角色是否作为本期可配置高级功能保留。

### 13 Review Access 的课程选择对 Organiser 不生效但仍可填写

`ReviewAccess.course` 对 organiser 使用当前 Module.organiserId，忽略其 courseIds。页面虽然注明课程选择用于其他审核角色，但选中 Organiser 后仍显示可编辑课程清单，也允许保存。管理员可能误以为已向教师授予某课程，实际并未生效。建议选中教师时显示只读的负责课程及入口提示；其他审核角色才编辑课程授权。

## 三 本次检查没有发现需要认定为错误的部分

- My Assignments 是学生自身安排与回复页面，不是管理分配的越权入口。
- 现有公开注册创建 Applicant；Teacher/Admin 由管理员创建。用户提供的两个测试职员账号是通过管理接口建立。
- Applicant 的 CSV 菜单已移除，页面和共享 CSV API 有拒绝检查；磁盘上残留 applicant/csv.jsp 可清理，但不等于当前仍可使用。
- PrivateDocuments 使用活跃账号、review.read、当前课程和已提交附件引用做检查；本轮未发现上述消息资料查询式的对象访问缺口。
- Section/正式分配使用当前课程归属做检查；第 3 项的问题是与旧职位归属不一致，不是所有课程边界都失效。
- Admin 拥有全局课程和审批权限，是现有角色模型的设计；不能只因其权限多就称为越权。
- CSV 导出内部审核备注另外检查 review.note，本轮没有发现 Viewer 可以借导出直接拿到内部备注的证据。

## 四 建议处理顺序

1. 修复消息 user-info 对象访问；统一教师读取/操作申请的审核授权。
2. 修复课程负责人交接与新旧所有权不一致。
3. 把批准/拒绝统一为可追溯的正式决定，避免两个入口相互矛盾。
4. 限制 Data Editor 的账号修改，复用合法角色及本人保护规则。
5. 按已讨论的方向集中 Admin CSV 入口，明确非管理员导出政策。
6. 在上述规则统一后实现教师直选；再简化角色登录、Viewer字段、授权页面和命名。

此文档是当前检查发现清单，不保证已经覆盖所有安全问题。没有为了复现而改变本地 PostgreSQL 的真实账号、权限或课程归属。
