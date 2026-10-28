# BioSci 项目功能范围核对

> 本文记录移除前的范围检查。同日已按用户要求移除面试业务功能，当前状态见 [面试移除说明](interview-retirement.md)。

核对日期：2026-10-27。范围：当前工作区的 JSP 菜单、页面、Servlet、服务及主要数据模型；包括尚未提交的前端和权限改动。本轮仅静态检查，没有删除功能，没有执行运行时验收。

## 需求基准

- Sponsor Proposal：`Capstone Proposal TA Application Management System - Christine Hsieh.pdf`。
- Business Case：`SWE277P/BioSci_Business_Case_Fall_2026.pdf`，2026-10-17。
- Project Charter：`SWE277P/BioSci_Project_Charter_Fall_2026.pdf`，尤其第 4 节 Project Scope。
- 三份源材料位于 `/Users/mac/Desktop/studyeeeee/Master/school/Fall2/Capstone/` 下。

核心流程是：开放周期及课程/Section/职位 → 学生提交资格与时间 → 授权人员筛选、查看可解释匹配和内部记录 → 人工选择/分配 → 冲突与重复检查 → 导出名册。

下面的“建议移除”是代码与现有书面范围比对后的建议，不代表 sponsor 已批准删除。未列入需求的辅助功能也不自动等于明确禁止的功能。Proposal 中的 stakeholder interviews 指团队访谈需求相关者，不是系统为 TA 候选人安排面试。

## 一 优先移除或退出业务演示的候选

| 功能 | 源码证据 | 比对结论与处理建议 |
| --- | --- | --- |
| TA 候选人面试管理 | `backend/src/main/webapp/organiser/interviews.jsp`；`OrganiserServlet` 的 interviews GET/POST/PUT/DELETE；`OrganiserService.getInterviews/createInterview/updateInterview/deleteInterview`；`model/Interview.java` | 三份材料没有要求候选人面试预约、改期、视频/现场类型或面试记录。建议移出本期产品，保留必要历史数据兼容。 |
| Pipeline 中的面试阶段及自动打开预约框 | `organiser/pipeline.jsp` 的 applied/review/shortlisted/interview/offer 列、拖入 interview 的预约逻辑；`OrganiserService` 的状态与通知处理 | 选拔状态跟踪必须保留，但固定面试流程没有需求依据。建议统一到 Review Workspace 的推荐、候补、批准、不选用，避免维护两套审核操作。先核对旧接口调用和状态映射，不直接删除所有 Pipeline 后端方法。 |
| Dashboard 的面试卡片、面试日历与活动 | `applicant/dashboard.jsp` 的 Interviews Scheduled、interview events；`ApplicantService.getDashboard`；`OrganiserService.getDashboard` | 是面试模块的连带界面。移除面试时一并清理；正式 Section 安排的工作日历仍应保留。 |
| 通用 Data Editor | `admin/data-editor.jsp` 自称 full CRUD；`AdminServlet` 的 data-editor 路由；`AdminService` 的 collection 数据访问与修改 | 核心验收要求能通过业务界面完成流程，不要求管理员直接编辑集合。建议退出正常管理菜单，并关闭相应业务环境入口；已有代码有部分历史保护，不能笼统声称所有校验都被绕过。测试数据管理可转为独立开发工具。 |
| 完整聊天系统 | Applicant/Organiser/Admin 的 messages.jsp；`MessageServlet`、`MessageService`；共用消息脚本含编辑、撤回、附件等 | 完整点对点会话、消息编辑/撤回和聊天附件没有明确 MVP 要求。建议本期隐藏聊天入口并停止扩展。申请状态通知及内部审核备注不属于聊天，应保留。 |

## 二 适合精简或等待 sponsor 确认

| 功能 | 证据与判断 | 建议 |
| --- | --- | --- |
| 登录欢迎信/登录提醒等 SMTP 邮件模块 | `service/EmailService.java` 包含 SMTP 配置和发送模板，Main、Listener、AuthServlet 有初始化引用；当前 AuthServlet 未发现调用邮件发送方法 | 可列为遗留集成候选，不应描述成当前确实会发邮件。先保留站内事件通知；是否需要正式外发邮件由 sponsor 确认。 |
| Platform Analytics 的趋势图、课程申请量图 | `admin/dashboard.jsp` 的 Application Trends 与 Applicants per Module | 属于可选展示增强。可压缩为关键计数；不能连同“拟分配、已填、未填名额”核心可视性一起删除。 |
| Properties 中跨部门组织结构配置和静态角色介绍 | `admin/properties.jsp` 的 Global Terms、Departments & Faculties、Role Designations | 本期可以固定 BioSci 组织范围、简化设置入口。Term 与申请周期存在关联；账号角色和课程授权本身属于核心需求，不可整块删除。静态三角色说明还应与现有六角色体系同步。 |
| Profile 与 TA Application Profile 的重复字段 | `applicant/profile.jsp` 与 `applicant/application-profile.jsp` 都包含身份/学术信息、技能/经历；旧 Profile 另有 GPA | 建议统一可编辑信息来源，或把 Profile 定位为账号/附件资料，TA Application Profile 保留 sponsor 表单。GPA 没有在提供材料中列为明确必填或匹配规则，应确认后再决定；不要直接删除整个 Profile。 |
| 旧 Workload 手动 reassign 与旧 workloadAssignments 数据路径 | `AdminServlet` 的 workload/{id}/reassign；`AdminService.reassignWorkload`、workloadAssignments 集合操作 | 新正式 SectionAssignment 台账已有周期、发布、回复和历史。旧入口属于重复业务写入候选；代码对已有分配历史有 409 保护。建议调查调用方后弃用旧写入入口，保留统一工时统计。 |
| 学生接受/拒绝、回复截止时间及自动释放名额 | `SectionAssignmentService`、`assignments.js` | 属于对核心分配记录的额外流程细化，书面要求未规定这套完整 offer-response 流程。不是单凭 My Assignments 页面就能判定的越权。可保留现有实现，但是否作为验收必选需 sponsor 确认；若精简必须同步容量、工时、通知和历史规则。 |

## 三 应保留的核心或必要支撑

- Application Periods、课程/Section/职位管理、学生 TA Application Profile、Weekly Availability。
- Applicant 列表、检索/筛选、内部备注、候补/推荐/人工决定与变更记录。
- Matching Criteria、资格与时间匹配解释；目前 `CriteriaMatchingService` 是规则评分，不能因为显示 score 就当成禁止的自主 AI 选人。
- TA Assignments、个人 My Assignments、名额状态、重复/冲突检查和基于正式分配的工时统计。
- CSV 名册导出明确在范围内。Sponsor CSV 导入属于支持范围，是否本期必选仍结合 sponsor 确认；不要因“CSV”就全部删除。Applicant CSV 页面当前仍在磁盘上，但入口已移除且 AuthFilter 拒绝其访问，属于可清理遗留页面。
- Users、Review Access、认证与课程授权、私有附件访问、审计/系统日志、备份恢复。这些不是原 proposal 每项都逐字列明的业务页面，但支撑访问控制、可追踪性和交付维护。
- 基础状态通知、申请提交快照。它们与完整即时聊天或面试通知需要分开处理。

## 四 明确范围外 但未在本轮确认存在可运行功能

真实学生记录导入、UCI 生产 SSO/部署、工资/HR/聘用合同/appointment letters、自主最终选人、复杂全局排课优化，以及未经批准的付费 SaaS。不要把这些写成“项目已经实现的多余功能”。

## 五 文档与当前实现的偏差

Charter 描述 JSON/可评估 SQLite 的初始方案，当前源码采用 PostgreSQL、JDBC 和迁移。旧架构设计脚本还描述 React/FastAPI。技术文档需要更新，但 PostgreSQL 本身不能据此判为多余功能，也没有依据要求本轮回退数据库。

按 Charter 时间表，10 月 26 日至 11 月 6 日是 Core implementation 阶段；前期任务清单里的 Design and Foundation 日期已经结束。该日程仍是规划目标，不是 sponsor 确认的实际进度。

## 六 推荐清理顺序

1. 面试模块及其双方 Dashboard、通知、申请状态和 Pipeline 耦合。
2. 将重复审核入口统一到 Review Workspace，先明确旧状态到新状态的映射。
3. 将 Data Editor 从日常业务环境退出；单独处理页面和 API。
4. 隐藏完整聊天功能，保留状态通知和内部审核备注。
5. 合并重复个人资料字段，简化非核心统计及组织设置。
6. 最后再决定外发邮件、学生回复截止流程等待确认扩展；保留历史记录，更新回归用例与手册。

Interview 涉及 model、数据库/种子数据、API、service、双方 Dashboard、Pipeline、详情页、通知、前端 api helper、测试和手册。只删菜单不会删除功能；直接删表或删除历史状态也不是本轮建议的第一步。
