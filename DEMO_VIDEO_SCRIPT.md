# TA&MO Demo Video Script (≤ 10 minutes)

> Target length: **9 min 30 sec – 10 min 00 sec**
> Speaking pace: ~140 WPM (slow & clear for marking).
> Total narration words: ~1350.
> Recording tip: 提前在浏览器登好 3 个标签页（Applicant / Organiser / Admin），录制时直接切换 tab，避免每次重新登录浪费时间。
> Recording tip: 在录前用 admin 把 `data.json` 备份一份；演示完误删等错误处理后可以恢复。

---

## Pre-Recording Checklist

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | `mvn compile exec:java` 已启动，控制台显示 `TA&MO 后端已启动: http://localhost:8080/tamo` | 服务必须先跑起来 |
| 2 | 浏览器窗口最大化，缩放 100%，关闭所有其他 tab、扩展通知 | 画面干净 |
| 3 | 麦克风测试：环境安静，无回声 | 录音必须有人声讲解 |
| 4 | 在 3 个浏览器窗口分别预登录：`alex.j@university.edu` / `alan@university.edu` / `admin@university.edu`（密码：applicant/organiser 全部用 `demo123`，admin 用 `admin123`），但**录视频时仍从 login 页演示一次登录** | 节省切换时间 |
| 5 | 准备好一个 PDF 文件（任意 < 5 MB）用于上传 CV 演示 | 文件可重命名为 `Demo_CV.pdf` |
| 6 | 全程用 **OBS / QuickTime** 录屏 + 系统麦克风 | 满足 "voice must be recorded" |

---

## Scene 1 — Opening Hook (0:00 – 0:25)

**画面操作**：停留在登录页 `http://localhost:8080/tamo/auth/login.jsp`，背景动画在播放。

**Narration (English):**
> "Hi everyone. Today I'm going to introduce **TA&MO**, a full-stack Teaching Assistant recruitment and management platform our team built for the EBU 6304 Software Engineering project. TA&MO connects three groups of users — applicants, organisers, and administrators — into a single workflow that turns a messy hiring process into a clean, trackable pipeline. The whole system runs on Java Servlets with embedded Tomcat, served through eighteen JSP pages and forty-two REST endpoints. Let's see it in action."

---

## Scene 2 — Landing Page & Login (0:25 – 1:10)

**画面操作**：
1. 打开 `http://localhost:8080/tamo/index.jsp` — 滚动一下展示落地页。(5s)
2. 点 "Log In" 进入登录页。
3. **故意先输入错误密码** `wrong@xx.com / 12345`，role 选 Applicant，点 Log In → 出现红色错误提示。(8s)
4. 改回正确账号 `alex.j@university.edu / demo123`，role = Applicant，登录成功。

**Narration (English):**
> "We start at the public landing page. The site uses a modern Tailwind interface with animated visuals to give a polished first impression. Clicking *Log In* opens the authentication page, where I can pick one of the three roles. Before I sign in, let me show you the error handling: if I type the wrong credentials, the back-end returns a 401, the cat animation plays a fail state, and an inline red banner explains the issue without ever crashing the page. Now I'll log in correctly as the applicant **Alex Johnson** — and you can see the role-aware redirect drops me straight onto the applicant dashboard."

---

## Scene 3 — Applicant Role (1:10 – 3:30)

### 3.1 Dashboard (1:10 – 1:30)

**画面操作**：停留 5s 展示统计卡片（Total Apps / Pending / Profile Completion）、最近申请、推荐岗位。

**Narration:**
> "This is the applicant dashboard. At a glance Alex sees how many applications he has, how many are still pending, his profile completion percentage, and the recommended jobs surfaced for his department. Every number is computed live by the `/api/applicant/dashboard` endpoint."

### 3.2 Browse & Search Jobs (1:30 – 2:00)

**画面操作**：
1. 左侧菜单点 **Jobs**。
2. 在搜索框输入 `data` —— 列表实时筛选（debounce 300ms）。
3. 用 status filter 切换 *Active* / *All*。
4. 点击其中一张 job 卡片 → 进入 Job Detail 页。

**Narration:**
> "Moving to *Browse Jobs*, applicants can full-text search across role titles and module codes. The search input is debounced, so the API isn't hammered on every keystroke. The status drop-down filters between active and closed postings. I'll click into a *Data Structures* TA role to see the full description, requirements, weekly hours, and the deadline."

### 3.3 Apply (with error) (2:00 – 2:30)

**画面操作**：
1. 在 Job Detail 点 **Apply Now** → 弹窗确认 → 提交成功 toast。
2. 立即再点一次 Apply → **出现 "You have already applied" 错误提示**（错误处理）。

**Narration:**
> "Submitting an application takes one click, and the back-end immediately writes the record into `data.json` through our thread-safe `JsonDataService`. Now watch this — if Alex accidentally clicks *Apply* again on the same posting, the server detects the duplicate and rejects it with a clear message. That kind of guard rail matters when hundreds of TAs are competing for the same module."

### 3.4 My Applications & Status Tracking (2:30 – 2:50)

**画面操作**：
1. 菜单点 **My Applications** → 展示列表（带状态徽章：applied / review / shortlisted / interview / offer / rejected）。
2. 点其中一个进入详情，展示 timeline。

**Narration:**
> "Under *My Applications*, every submission is tracked across six pipeline stages, each with a colour-coded badge. Opening one shows the full timeline so the applicant always knows where they stand — no more emailing the professor to ask 'did you receive my CV?'."

### 3.5 Profile & CV Upload (2:50 – 3:20)

**画面操作**：
1. 菜单 **Profile**。
2. 编辑 GPA / Skills 字段，点 Save → toast 成功。
3. 在 Documents 区块点 **Upload** → 选择本地 `Demo_CV.pdf` → 上传成功，列表新增一行。
4. 点新文档旁边的删除按钮，确认弹窗 → 删除。

**Narration:**
> "The profile page is the applicant's living CV. Personal info, GPA, coursework, and prior experience can all be edited inline and saved without a page reload. In the documents section I can upload a CV — it's stored as Base-64 in our JSON store, so no external file server is required — and I can also remove a document with a confirmation prompt that prevents accidental deletion."

### 3.6 Messages (3:20 – 3:30)

**画面操作**：菜单 **Messages**，展示与 organiser 的对话记录。(快速展示 8 秒)

**Narration:**
> "Finally, the built-in messaging inbox lets applicants and organisers communicate without leaving the platform. Now let me log out and switch to the organiser view."

---

## Scene 4 — Organiser Role (3:30 – 6:15)

**画面操作**：右上角 logout（或直接打开预登录的 Organiser 浏览器窗口）。
切换登录账号 `alan@university.edu / demo123`，role = Organiser。

### 4.1 Dashboard (3:30 – 3:45)

**画面操作**：展示数据卡片（My Open Jobs / Pending Reviews / Upcoming Interviews）。

**Narration:**
> "I'm now Dr. Alan Turing, an organiser. His dashboard summarises his open postings, pending application reviews, and upcoming interviews — exactly the four numbers a busy professor cares about on a Monday morning."

### 4.2 Create Job — with validation error (3:45 – 4:25)

**画面操作**：
1. 菜单 **Manage Jobs** → 点 **+ New Job**。
2. **故意只填部分字段**（比如不填 module）→ 点 Save → 出现红色字段提示 "Module is required"。(错误处理)
3. 补全所有字段：Title=`TA - Linear Algebra`, Module 选 `MATH201`, Department=`Mathematics`, Term=`Fall 2026`, Weekly Hours=`6`, Positions=`2`, Description 填一段简介（"Support MATH201 tutorials and mark weekly problem sets."），Deadline 选未来日期（比如 `2026-12-30`） → Save。
4. 列表里出现新岗位。

**Narration:**
> "From *Manage Jobs* a professor can post, edit, or close a TA position. Notice the front-end validation — if I leave a required field empty, the form blocks submission and highlights what's missing, so we never POST half-formed data to the API. Now I'll fill the form properly: title, module, term, weekly hours, description, and deadline. Save, and the job appears in the table immediately, ready for applications."

### 4.3 Recruitment Pipeline (Kanban) (4:25 – 5:25)

**画面操作**：
1. 菜单 **Pipeline** → 展示六列 Kanban：Applied / Review / Shortlisted / Interview / Offer / Rejected。
2. 用鼠标把某个 Applied 状态的卡片拖到 **Review** 列 → toast 成功。
3. 把另一张卡拖到 **Interview** 列 → 弹出 *Schedule Interview* 模态框：填写日期、时间、地点 → 保存。
4. **故意把一张已经在 Offer 列的卡拖回 Applied** → 出现 toast "Cannot move candidate backwards once they are in a later stage."（错误处理）

**Narration:**
> "Here's the heart of the organiser experience: a six-column Kanban board representing the recruitment pipeline. I can drag any candidate card to a new stage and the change is persisted instantly. When I move a candidate into the *Interview* column, a scheduling modal pops up asking for date, time, and location — so an interview record is created in the same gesture. And here's an important business rule: candidates can never be moved *backwards* in the pipeline. Watch what happens when I try — the system blocks the move and shows a clear toast. This protects audit integrity."

### 4.4 Candidate Detail & Accept / Reject (5:25 – 5:50)

**画面操作**：
1. 点击 Pipeline 中某张卡片 → 滑出右侧详情面板，展示 CV、GPA、coursework、申请理由。
2. 点 **Accept** 或 **Reject** 按钮 → 状态更新。

**Narration:**
> "Clicking a candidate card slides out a side panel with their CV, GPA, coursework breakdown, and motivation. From here Dr. Turing can accept or reject with one click — much faster than emailing back and forth."

### 4.5 Interviews List (5:50 – 6:15)

**画面操作**：
1. 菜单 **Interviews** → 列表显示已安排面试。
2. 编辑其中一条面试时间 → 保存。

**Narration:**
> "All scheduled interviews live in the *Interviews* page, sorted by date. I can edit, reschedule, or cancel any interview, and the corresponding application timeline is updated automatically. Now let's see the platform from the most powerful seat — the administrator."

---

## Scene 5 — Admin Role (6:15 – 9:15)

**画面操作**：登出，登录 `admin@university.edu / admin123`，role = Admin。

### 5.1 Analytics Dashboard (6:15 – 6:50)

**画面操作**：展示统计卡 + 两张 Chart.js 图表（Application Trend area chart, Status Breakdown bar chart）+ 系统状态告警条。

**Narration:**
> "The admin dashboard is a system-wide control tower. Three stat cards show total applicants, active postings, and the offer rate. Below them, two live Chart.js graphs visualise the application trend over time and the distribution across pipeline stages. The red status bar at the top monitors back-end health — if `data.json` ever becomes unwritable, we'd see it here first."

### 5.2 User Management (6:50 – 7:30)

**画面操作**：
1. 菜单 **Users** → 列表展示所有 10 个用户。指给观众看 Edward Park 已经是 *suspended* 状态。
2. 用 role filter 切到 *Organiser* → 列表过滤为 3 个 organiser。再切回 *All*。
3. 点 **+ Add User** → 填写：Name=`Dr. Ada Lovelace`, Email=`ada@university.edu`, Password=`demo123`, Role=`Organiser`, Department=`Computer Science` → Save。新用户出现在列表。
4. 点新用户的编辑按钮 → 修改 status 为 *suspended* → 保存。
5. 点删除按钮 → **弹出二次确认 modal**（错误处理：防误删） → 取消。

**Narration:**
> "Under *User Management*, the admin can search, filter by role, create, edit, suspend, or delete any account. Critical destructive actions like deletion are guarded by a second confirmation modal — a single misclick can't wipe a real user. Notice that suspended users immediately lose login access on their next attempt."

### 5.3 Module Management (7:30 – 7:50)

**画面操作**：
1. 菜单 **Modules** → 列表 + keyword search。
2. 新增一个 module：code=`EBU7000`, name=`AI for Education`, dept=`CS` → 保存。
3. 编辑或删除一个旧 module（演示编辑即可）。

**Narration:**
> "*Module Management* is the source of truth for every course code that a TA job can reference. The admin can add new modules, edit names, or retire obsolete ones — and these changes propagate to organisers' job-creation drop-downs in real time."

### 5.4 Properties — Terms & Departments (7:50 – 8:10)

**画面操作**：
1. 菜单 **Properties** → 两个 tab: Terms / Departments。
2. Terms tab 添加 term `Fall 2027` (startDate=`2027-09-15`, endDate=`2027-12-20`)。
3. Departments tab 添加 department `Materials Science`。

**Narration:**
> "*Properties* groups two key reference tables — academic terms and university departments. Centralising them here keeps job postings consistent across the whole platform."

### 5.5 Workload Management (8:10 – 8:40)

**画面操作**：
1. 菜单 **Workload** → 表格显示每个 TA 的 current/max weekly hours 与 utilisation bar。**Sarah Lee 18/20 (90%)** 应该显示红色高利用率，Diana Chen 0/20 (0%) 显示空闲。
2. 点 Sarah Lee 行的 **Reassign** → 把她在 EBU6304 的 6 小时减到 3 小时（或者把 EBU6304 那条工作转给 Diana）→ 保存。利用率条立刻刷新。

**Narration:**
> "*Workload Management* solves a classic university pain point: overworking some TAs while others sit idle. Each row shows how many hours a TA is currently committed to versus their cap, with a utilisation bar that goes red when they're overloaded. The admin can rebalance assignments with the *Reassign* action, and the affected TAs see the change in their dashboards instantly."

### 5.6 System Logs (8:40 – 9:00)

**画面操作**：
1. 菜单 **Logs** → 显示按时间倒序的操作日志。
2. 用 level filter 选 `ERROR` / `INFO` / `WARN`，再用关键字搜索某用户名。

**Narration:**
> "Every meaningful action — logins, application submissions, status changes, deletions — is recorded in the *System Logs*. Admins can filter by severity level or search by user, which is essential for both auditing and debugging."

### 5.7 Messages & Data Editor (9:00 – 9:15)

**画面操作**：
1. 菜单 **Messages** —— 快速展示。
2. 菜单 **Data Editor** —— 显示原始 JSON 编辑界面（如时间允许，演示读、不修改）。

**Narration:**
> "Admins also share the same in-app messaging inbox, plus a raw *Data Editor* for advanced JSON inspection — useful during initial deployment or schema migrations."

---

## Scene 6 — Architecture Recap & Closing (9:15 – 9:55)

**画面操作**：切到 README 文件或 IDE，简短停留在架构图 / 项目结构上 5–8 秒；最后回到登录页。

**Narration:**
> "Behind every screen you just saw is a clean three-layer architecture: JSP pages, Java Servlet REST endpoints protected by a session-based `AuthFilter`, and a single thread-safe JSON data store. Forty-two endpoints, eighteen pages, three roles, and zero external database dependencies — TA&MO is lightweight enough to run on a laptop, yet structured enough to scale into a real university deployment.

> Thank you for watching. **TA&MO** — recruiting the right TA, the right way. From Group 100, EBU 6304 Software Engineering."

---

## Error-Handling Coverage Summary (for marking scheme)

| # | Error case demonstrated | Scene |
|---|--------------------------|-------|
| 1 | Wrong password → red error banner + UI feedback | 2 |
| 2 | Duplicate job application blocked by server | 3.3 |
| 3 | Required-field validation on Create Job form | 4.2 |
| 4 | Pipeline drag-backwards prevention with toast | 4.3 |
| 5 | Delete-user double-confirmation modal | 5.2 |
| 6 | (Optional) suspended user cannot log in (mention only) | 5.2 |

---

## Timing Cheat Sheet

| Scene | Section | Start | End | Duration |
|-------|---------|-------|-----|----------|
| 1 | Opening | 0:00 | 0:25 | 0:25 |
| 2 | Landing + Login (with error) | 0:25 | 1:10 | 0:45 |
| 3 | Applicant — full flow | 1:10 | 3:30 | 2:20 |
| 4 | Organiser — full flow | 3:30 | 6:15 | 2:45 |
| 5 | Admin — full flow | 6:15 | 9:15 | 3:00 |
| 6 | Recap & closing | 9:15 | 9:55 | 0:40 |
| **Total** | | | | **≈ 9:55** |

> Buffer of 5 seconds is intentional — leaves room for cursor pauses without exceeding 10 minutes.

---

## Recording Workflow Tips

1. **Rehearse once** end-to-end with the script open on a second monitor or printed sheet — aim to finish at 9:30–9:50.
2. **Don't start narration until the page has loaded** — silent wait kills pacing; either pre-load or cut in editing.
3. If you're not confident speaking live, record **screen first**, then record **voice-over separately** and align in OBS / Premiere / iMovie.
4. **Reset demo data between rehearsals**: before recording, create a baseline snapshot once with `cp backend/src/main/resources/data.json backend/src/main/resources/data.demo.bak`. Between takes, restore it with `cp backend/src/main/resources/data.demo.bak backend/src/main/resources/data.json` so the pipeline / dashboards always look the same on camera. If you want to revert to the **original** dataset (pre-demo cleanup), use `cp backend/src/main/resources/data.json.original.bak backend/src/main/resources/data.json`.
5. Export at **1080p, ~30 fps, MP4 (H.264)** to keep file size reasonable for QM+ upload.
