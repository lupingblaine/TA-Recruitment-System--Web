<%@ page pageEncoding="UTF-8" %>
<script>
let currentTab = 'conversations';
let allContacts = [];
let allConversations = [];
let activeConversationId = null;
let activeReceiverId = null;
let pollInterval = null;
let isSending = false;
let lastMessageSnapshot = '';
let isComposing = false;
let replyTo = null;
let editingMessageId = null;
let lastMessages = [];
let contactsLoadError = null;

async function loadContacts() {
  contactsLoadError = null;
  if (typeof messageApi === 'undefined') {
    allContacts = [];
    contactsLoadError = 'api.js does not include messageApi (stale static/js/api.js on server; run ./deploy.sh full and hard-refresh cache)';
    return;
  }
  try {
    allContacts = await messageApi.getContacts();
  } catch (e) {
    allContacts = [];
    contactsLoadError = e.message || ('HTTP ' + (e.status || ''));
    console.error('loadContacts failed', e);
  }
}

async function loadConversations() {
  try { allConversations = await messageApi.getConversations(); } catch(e) { allConversations = []; }
}

function switchTab(tab) {
  currentTab = tab;
  document.getElementById('tabConversations').className = tab === 'conversations'
    ? 'flex-1 py-2.5 text-xs font-medium text-zinc-900 border-b-2 border-zinc-900'
    : 'flex-1 py-2.5 text-xs font-medium text-zinc-500 border-b-2 border-transparent hover:text-zinc-700';
  document.getElementById('tabContacts').className = tab === 'contacts'
    ? 'flex-1 py-2.5 text-xs font-medium text-zinc-900 border-b-2 border-zinc-900'
    : 'flex-1 py-2.5 text-xs font-medium text-zinc-500 border-b-2 border-transparent hover:text-zinc-700';
  renderLeftPanel();
}

function filterContacts() { renderLeftPanel(); }

function renderLeftPanel() {
  const panel = document.getElementById('leftPanel');
  const query = (document.getElementById('contactSearch').value || '').toLowerCase();

  if (currentTab === 'conversations') {
    let items = allConversations;
    if (query) items = items.filter(c => c.otherUserName.toLowerCase().includes(query));
    if (items.length === 0) {
      panel.innerHTML = '<div class="p-4 text-center text-zinc-400 text-sm">No conversations yet</div>';
      return;
    }
    panel.innerHTML = items.map(c => {
      const isActive = activeConversationId === c.conversationId;
      const initial = (c.otherUserName || 'U').charAt(0).toUpperCase();
      const unread = c.unreadCount > 0 ? '<span class="ml-auto h-5 min-w-5 rounded-full bg-blue-500 text-white text-xs flex items-center justify-center px-1">' + c.unreadCount + '</span>' : '';
      const time = formatTime(c.lastTimestamp);
      const roleBadge = getRoleBadge(c.otherUserRole);
      return '<div onclick="openConversation(\'' + c.conversationId + '\',\'' + c.otherUserId + '\',\'' + escapeAttr(c.otherUserName) + '\',\'' + c.otherUserRole + '\')" class="flex items-center gap-3 px-4 py-3 cursor-pointer transition-colors ' + (isActive ? 'bg-zinc-100' : 'hover:bg-zinc-50') + '">' +
        '<div onclick="event.stopPropagation();showUserCard(\'' + c.otherUserId + '\')" class="h-10 w-10 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600 shrink-0 hover:ring-2 hover:ring-blue-300 transition-all" title="View profile">' + initial + '</div>' +
        '<div class="flex-1 min-w-0"><div class="flex items-center gap-2"><span class="font-medium text-sm text-zinc-900 truncate">' + escapeHtml(c.otherUserName) + '</span>' + roleBadge + '</div>' +
        '<p class="text-xs text-zinc-500 truncate mt-0.5">' + escapeHtml(c.lastMessage || '') + '</p></div>' +
        '<div class="flex flex-col items-end gap-1 shrink-0"><span class="text-xs text-zinc-400">' + time + '</span>' + unread + '</div></div>';
    }).join('');
  } else {
    let items = allContacts;
    if (query) items = items.filter(c => c.name.toLowerCase().includes(query));
    if (items.length === 0) {
      const hint = contactsLoadError
        ? escapeHtml(contactsLoadError)
        : 'No contacts available for this role (applicants need applications; Teachers need applicants on their jobs)';
      panel.innerHTML = '<div class="p-4 text-center text-zinc-400 text-sm">' + hint + '</div>';
      return;
    }
    panel.innerHTML = items.map(c => {
      const initial = (c.name || 'U').charAt(0).toUpperCase();
      const roleBadge = getRoleBadge(c.role);
      return '<div onclick="startChat(\'' + c.id + '\',\'' + escapeAttr(c.name) + '\',\'' + c.role + '\')" class="flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-zinc-50 transition-colors">' +
        '<div onclick="event.stopPropagation();showUserCard(\'' + c.id + '\')" class="h-10 w-10 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600 shrink-0 hover:ring-2 hover:ring-blue-300 transition-all" title="View profile">' + initial + '</div>' +
        '<div class="flex-1 min-w-0"><div class="flex items-center gap-2"><span class="font-medium text-sm text-zinc-900 truncate">' + escapeHtml(c.name) + '</span>' + roleBadge + '</div>' +
        '<p class="text-xs text-zinc-500 mt-0.5">' + escapeHtml(c.department || '') + '</p></div></div>';
    }).join('');
  }
}

function getRoleBadge(role) {
  const colors = { admin: 'bg-purple-100 text-purple-700', organiser: 'bg-blue-100 text-blue-700', applicant: 'bg-emerald-100 text-emerald-700' };
  const cls = colors[role] || 'bg-zinc-100 text-zinc-600';
  return '<span class="text-xs px-1.5 py-0.5 rounded ' + cls + '">' + displayRole(role) + '</span>';
}

function formatTime(ts) {
  if (!ts) return '';
  try {
    const d = new Date(ts);
    const now = new Date();
    if (d.toDateString() === now.toDateString()) return d.getHours().toString().padStart(2,'0') + ':' + d.getMinutes().toString().padStart(2,'0');
    return (d.getMonth()+1) + '/' + d.getDate();
  } catch(e) { return ''; }
}

function openConversation(convId, otherUserId, otherUserName, otherUserRole) {
  activeConversationId = convId;
  activeReceiverId = otherUserId;
  lastMessageSnapshot = '';
  cancelReply();
  cancelEdit();
  setChatHeader(otherUserName, otherUserRole);
  document.getElementById('inputArea').classList.remove('hidden');
  loadMessages();
  startPolling();
  renderLeftPanel();
}

function startChat(userId, userName, userRole) {
  activeReceiverId = userId;
  const myId = getCurrentUserId();
  activeConversationId = myId < userId ? myId + '_' + userId : userId + '_' + myId;
  lastMessageSnapshot = '';
  cancelReply();
  cancelEdit();
  setChatHeader(userName, userRole);
  document.getElementById('inputArea').classList.remove('hidden');
  loadMessages();
  startPolling();
  switchTab('conversations');
}

function getCurrentUserId() {
  return document.querySelector('meta[name="uid"]')?.content || '';
}

function findUserName(userId) {
  const c = allContacts.find(x => x.id === userId);
  if (c) return c.name;
  const conv = allConversations.find(x => x.otherUserId === userId);
  if (conv) return conv.otherUserName;
  return 'User';
}

function setChatHeader(name, role) {
  const initial = (name || 'U').charAt(0).toUpperCase();
  const roleBadge = getRoleBadge(role);
  const uid = activeReceiverId || '';
  document.getElementById('chatHeader').innerHTML =
    '<div onclick="showUserCard(\'' + escapeAttr(uid) + '\')" class="h-9 w-9 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600 cursor-pointer hover:ring-2 hover:ring-blue-300 transition-all" title="View profile">' + initial + '</div>' +
    '<div class="cursor-pointer" onclick="showUserCard(\'' + escapeAttr(uid) + '\')"><div class="flex items-center gap-2"><span class="font-semibold text-zinc-900 hover:text-blue-600 transition-colors">' + escapeHtml(name) + '</span>' + roleBadge + '</div></div>';
  lucide.createIcons();
}

function messageSnapshot(messages) {
  return (messages || []).map(m => m.id + ':' + (m.edited ? '1' : '0') + ':' + (m.recalled ? '1' : '0')).join('|');
}

async function loadMessages(opts) {
  opts = opts || {};
  if (!activeConversationId) return;
  try {
    const messages = await messageApi.getMessages(activeConversationId);
    lastMessageSnapshot = messageSnapshot(messages);
    lastMessages = messages;
    renderMessages(messages, !!opts.scrollToBottom);
    await messageApi.markRead(activeConversationId);
    loadConversations().then(renderLeftPanel);
  } catch(e) {
    document.getElementById('messagesArea').innerHTML = '<div class="text-center text-zinc-400 text-sm">Failed to load messages</div>';
  }
}

function renderReplyQuote(m) {
  if (!m.replyToId || !m.replyToContent) return '';
  return '<div class="mb-2 px-3 py-2 rounded-lg bg-black/10 border-l-2 border-zinc-400 text-xs">' +
    '<p class="font-medium opacity-80">' + escapeHtml(m.replyToSenderName || 'User') + '</p>' +
    '<p class="opacity-70 truncate">' + escapeHtml(m.replyToContent) + '</p></div>';
}

function messageTextContent(m) {
  if (!m || !m.content) return '';
  return String(m.content).trim();
}

function renderTextParagraph(text, extraClass) {
  if (!text) return '';
  return '<p class="text-sm break-words' + (extraClass ? ' ' + extraClass : '') + '">' + escapeHtml(text) + '</p>';
}

function renderMessageBody(m) {
  if (m.recalled) {
    return '<p class="text-sm italic opacity-70">Message recalled</p>';
  }
  const text = messageTextContent(m);
  let html = renderReplyQuote(m);
  if (m.messageType === 'image' && m.attachmentUrl) {
    html += '<a href="' + escapeAttr(m.attachmentUrl) + '" target="_blank" rel="noopener"><img src="' + escapeAttr(m.attachmentUrl) + '" alt="image" class="max-w-full rounded-lg max-h-64 object-contain"></a>';
    if (text) html += renderTextParagraph(text, 'mt-2');
  } else if (m.attachmentUrl) {
    html += '<a href="' + escapeAttr(m.attachmentUrl) + '" target="_blank" rel="noopener" class="flex items-center gap-2 text-sm underline">' +
      '<i data-lucide="file" class="h-4 w-4"></i><span>' + escapeHtml(m.attachmentName || 'Download file') + '</span></a>';
    if (text) html += renderTextParagraph(text, 'mt-2');
  } else if (text) {
    html += renderTextParagraph(text);
  }
  return html;
}

function renderMessageActions(m, isMine) {
  if (!isMine || m.recalled) return '';
  let btns = '<div class="flex gap-1 mt-1 opacity-0 group-hover:opacity-100 transition-opacity">';
  btns += '<button type="button" onclick="startReply(\'' + escapeAttr(m.id) + '\')" class="text-xs text-zinc-500 hover:text-zinc-800 px-1">Reply</button>';
  if (m.canEdit) btns += '<button type="button" onclick="startEdit(\'' + escapeAttr(m.id) + '\')" class="text-xs text-zinc-500 hover:text-zinc-800 px-1">Edit</button>';
  if (m.canRecall) btns += '<button type="button" onclick="recallMsg(\'' + escapeAttr(m.id) + '\')" class="text-xs text-red-500 hover:text-red-700 px-1">Recall</button>';
  return btns + '</div>';
}

function renderMessages(messages, scrollToBottom) {
  const area = document.getElementById('messagesArea');
  const myId = getCurrentUserId();

  if (!messages || messages.length === 0) {
    area.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-zinc-400"><p class="text-sm">No messages yet</p><p class="text-xs mt-1">Send the first message to start the conversation</p></div>';
    return;
  }

  let html = '';
  let lastDate = '';
  messages.forEach(m => {
    const msgDate = m.timestamp ? m.timestamp.substring(0, 10) : '';
    if (msgDate !== lastDate) {
      lastDate = msgDate;
      html += '<div class="flex items-center gap-3 my-4"><div class="flex-1 h-px bg-zinc-200"></div><span class="text-xs text-zinc-400 shrink-0">' + msgDate + '</span><div class="flex-1 h-px bg-zinc-200"></div></div>';
    }
    const isMine = m.senderId === myId;
    const time = formatTime(m.timestamp) + (m.edited ? ' · edited' : '');
    const body = renderMessageBody(m);
    const actions = renderMessageActions(m, isMine);
    const senderInitial = findUserName(m.senderId).charAt(0).toUpperCase();
    if (isMine) {
      html += '<div class="flex justify-end group"><div class="flex flex-col items-end max-w-[75%]"><div class="msg-bubble bg-zinc-900 text-white px-4 py-2.5 rounded-2xl rounded-br-md">' + body + '</div>' +
        '<div class="text-xs text-zinc-400 mt-1">' + time + '</div>' + actions + '</div></div>';
    } else {
      html += '<div class="flex justify-start gap-2 group">' +
        '<div onclick="showUserCard(\'' + escapeAttr(m.senderId) + '\')" class="h-8 w-8 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600 text-xs shrink-0 mt-1 cursor-pointer hover:ring-2 hover:ring-blue-300 transition-all" title="View profile">' + senderInitial + '</div>' +
        '<div class="flex flex-col items-start max-w-[70%]"><div class="msg-bubble bg-zinc-100 text-zinc-900 px-4 py-2.5 rounded-2xl rounded-bl-md">' + body + '</div>' +
        '<div class="text-xs text-zinc-400 mt-1">' + time + '</div>' +
        '<button type="button" onclick="startReply(\'' + escapeAttr(m.id) + '\')" class="text-xs text-zinc-500 hover:text-zinc-800 mt-1 opacity-0 group-hover:opacity-100">Reply</button></div></div>';
    }
  });

  const scrollTop = area.scrollTop;
  area.innerHTML = html;
  if (scrollToBottom) {
    area.scrollTop = area.scrollHeight;
  } else {
    area.scrollTop = scrollTop;
  }
  lucide.createIcons();
}

function startReply(messageId) {
  const m = lastMessages.find(x => x.id === messageId);
  if (!m || m.recalled) return;
  editingMessageId = null;
  document.getElementById('editBar').classList.add('hidden');
  replyTo = m;
  document.getElementById('replyBar').classList.remove('hidden');
  document.getElementById('replyBarTitle').textContent = 'Reply to ' + (m.senderId === getCurrentUserId() ? 'yourself' : findUserName(m.senderId));
  let preview = m.recalled ? '[Recalled]' : (m.messageType === 'image' ? '[Image]' : (m.messageType === 'file' ? ('[File] ' + (m.attachmentName || '')) : (m.content || m.attachmentName || '')));
  document.getElementById('replyBarPreview').textContent = preview;
  document.getElementById('messageInput').focus();
}

function cancelReply() {
  replyTo = null;
  document.getElementById('replyBar').classList.add('hidden');
}

function startEdit(messageId) {
  const m = lastMessages.find(x => x.id === messageId);
  if (!m || !m.canEdit) return;
  cancelReply();
  editingMessageId = messageId;
  document.getElementById('editBar').classList.remove('hidden');
  document.getElementById('messageInput').value = m.content || '';
  document.getElementById('messageInput').focus();
}

function cancelEdit() {
  editingMessageId = null;
  document.getElementById('editBar').classList.add('hidden');
  document.getElementById('messageInput').value = '';
}

async function recallMsg(messageId) {
  if (!confirm('Recall this message?')) return;
  try {
    await messageApi.recallMessage(messageId);
    loadMessages();
  } catch(e) {
    showToast(e.message || 'Recall failed', 'error');
  }
}

async function onFileSelected(event) {
  const file = event.target.files && event.target.files[0];
  event.target.value = '';
  if (!file || !activeReceiverId || isSending) return;
  isSending = true;
  try {
    const uploaded = await messageApi.uploadFile(file);
    const payload = {
      receiverId: activeReceiverId,
      content: '',
      messageType: uploaded.messageType,
      attachmentUrl: uploaded.url,
      attachmentName: uploaded.fileName,
      attachmentSize: uploaded.fileSize,
    };
    if (replyTo && replyTo.id) payload.replyToId = replyTo.id;
    await messageApi.sendMessage(payload);
    cancelReply();
    cancelEdit();
    document.getElementById('messageInput').value = '';
    loadMessages({ scrollToBottom: true });
  } catch(e) {
    showToast(e.message || 'Upload failed', 'error');
  } finally {
    isSending = false;
  }
}

async function sendMsg() {
  if (isSending) return;
  const input = document.getElementById('messageInput');
  const content = input.value.trim();
  if (!activeReceiverId) return;

  if (editingMessageId) {
    if (!content) { showToast('Message cannot be empty', 'error'); return; }
    isSending = true;
    try {
      await messageApi.editMessage(editingMessageId, content);
      cancelEdit();
      loadMessages();
    } catch(e) {
      showToast(e.message || 'Edit failed', 'error');
    } finally {
      isSending = false;
    }
    return;
  }

  if (!content) return;
  isSending = true;
  input.value = '';
  try {
    const payload = { receiverId: activeReceiverId, content: content };
    if (replyTo && replyTo.id) payload.replyToId = replyTo.id;
    await messageApi.sendMessage(payload);
    cancelReply();
    loadMessages({ scrollToBottom: true });
  } catch(e) {
    input.value = content;
    showToast(e.message || 'Failed to send message', 'error');
  } finally {
    isSending = false;
  }
}

function setupMessageInput() {
  const input = document.getElementById('messageInput');
  if (!input) return;

  input.addEventListener('compositionstart', () => { isComposing = true; });
  input.addEventListener('compositionend', () => { isComposing = false; });

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      if (e.isComposing || isComposing) return;
      e.preventDefault();
      sendMsg();
    }
  });
}

function startPolling() {
  if (pollInterval) clearInterval(pollInterval);
  pollInterval = setInterval(() => {
    if (activeConversationId) loadMessages();
  }, 3000);
}

function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.appendChild(document.createTextNode(str));
  return div.innerHTML;
}
function escapeAttr(str) {
  if (!str) return '';
  return str.replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/'/g,'&#39;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

async function showUserCard(userId) {
  if (!userId) return;
  try {
    const info = await messageApi.getUserInfo(userId);
    document.getElementById('ucAvatar').textContent = (info.name || '?').charAt(0).toUpperCase();
    document.getElementById('ucName').textContent = info.name || '-';
    const roleColors = { admin: 'bg-purple-500/20 text-purple-200', organiser: 'bg-blue-500/20 text-blue-200', applicant: 'bg-emerald-500/20 text-emerald-200' };
    document.getElementById('ucRole').innerHTML = '<span class="text-xs px-2 py-0.5 rounded-full ' + (roleColors[info.role] || 'bg-white/20 text-white/80') + '">' + displayRole(info.role) + '</span>';

    let basic = '<div class="space-y-1"><p class="text-xs text-zinc-500">Email</p><p class="font-medium text-zinc-900 text-sm truncate">' + escapeHtml(info.email || '-') + '</p></div>';
    basic += '<div class="space-y-1"><p class="text-xs text-zinc-500">Department</p><p class="font-medium text-zinc-900 text-sm">' + escapeHtml(info.department || '-') + '</p></div>';
    document.getElementById('ucBasic').innerHTML = basic;

    let extra = '';
    if (info.profile) {
      const p = info.profile;
      extra += '<div class="border-t border-zinc-100 pt-3 space-y-2">';
      extra += '<h4 class="text-xs font-semibold text-zinc-600 uppercase tracking-wide">Academic</h4>';
      extra += '<div class="grid grid-cols-3 gap-2 text-sm">';
      extra += '<div><p class="text-xs text-zinc-500">Degree</p><p class="font-medium text-zinc-900">' + escapeHtml(p.degree || '-') + '</p></div>';
      extra += '<div><p class="text-xs text-zinc-500">Year</p><p class="font-medium text-zinc-900">' + (p.year || '-') + '</p></div>';
      extra += '<div><p class="text-xs text-zinc-500">GPA</p><p class="font-medium text-zinc-900">' + (p.gpa ? p.gpa + '/' + (p.maxGpa || '4.0') : '-') + '</p></div>';
      extra += '</div>';
      if (p.skills && p.skills.length > 0) {
        extra += '<div class="mt-2"><p class="text-xs text-zinc-500 mb-1">Skills</p><div class="flex flex-wrap gap-1">' +
          p.skills.map(function(s) { return '<span class="text-xs px-2 py-0.5 bg-zinc-100 text-zinc-700 rounded-full border border-zinc-200">' + escapeHtml(s) + '</span>'; }).join('') + '</div></div>';
      }
      extra += '</div>';
    }

    if (info.applications && info.applications.length > 0) {
      extra += '<div class="border-t border-zinc-100 pt-3 space-y-2">';
      extra += '<h4 class="text-xs font-semibold text-zinc-600 uppercase tracking-wide">Applications</h4>';
      extra += info.applications.map(function(a) {
        var statusColors = { applied: 'badge-neutral', review: 'badge-neutral', shortlisted: 'badge-warning', offer: 'badge-success', rejected: 'badge-danger' };
        return '<div class="flex items-center justify-between text-sm py-1">' +
          '<div class="min-w-0"><p class="font-medium text-zinc-900 truncate">' + escapeHtml(a.jobTitle) + '</p>' +
          '<p class="text-xs text-zinc-500">' + escapeHtml(a.moduleCode) + '</p></div>' +
          '<span class="badge ' + (statusColors[a.status] || 'badge-neutral') + ' shrink-0 ml-2">' + a.status + '</span></div>';
      }).join('');
      extra += '</div>';
    }

    if (info.modules && info.modules.length > 0) {
      extra += '<div class="border-t border-zinc-100 pt-3 space-y-2">';
      extra += '<h4 class="text-xs font-semibold text-zinc-600 uppercase tracking-wide">Manages Modules</h4>';
      extra += info.modules.map(function(m) {
        return '<div class="flex items-center gap-2 text-sm py-1">' +
          '<span class="badge badge-neutral shrink-0">' + escapeHtml(m.code) + '</span>' +
          '<span class="text-zinc-700 truncate">' + escapeHtml(m.name || '') + '</span></div>';
      }).join('');
      extra += '</div>';
    }

    document.getElementById('ucExtra').innerHTML = extra;
    document.getElementById('userCardModal').classList.remove('hidden');
    lucide.createIcons();
  } catch (e) {
    showToast('Failed to load user info', 'error');
  }
}

function closeUserCard() {
  document.getElementById('userCardModal').classList.add('hidden');
}

async function initMessaging() {
  if (typeof messageApi === 'undefined') {
    contactsLoadError = contactsLoadError || 'api.js not loaded or outdated';
    renderLeftPanel();
    lucide.createIcons();
    return;
  }
  setupMessageInput();
  await Promise.all([loadContacts(), loadConversations()]);
  renderLeftPanel();
  lucide.createIcons();
}

function bootMessaging() {
  initMessaging();
}
bootMessaging();
</script>
