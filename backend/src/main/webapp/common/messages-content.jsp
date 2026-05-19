<%@ page pageEncoding="UTF-8" %>

<div class="flex h-[calc(100vh-8rem)] max-w-6xl mx-auto gap-0 rounded-2xl overflow-hidden border border-zinc-200 bg-white shadow-sm">
  <!-- Left Panel: Conversations -->
  <div class="w-80 border-r border-zinc-200 flex flex-col shrink-0">
    <div class="p-4 border-b border-zinc-100">
      <h2 class="text-lg font-bold text-zinc-900 mb-3">Messages</h2>
      <div class="relative">
        <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400"></i>
        <input id="contactSearch" type="text" placeholder="Search contacts..." class="input pl-10 text-sm" oninput="filterContacts()">
      </div>
    </div>
    <div class="flex border-b border-zinc-100">
      <button id="tabConversations" onclick="switchTab('conversations')" class="flex-1 py-2.5 text-xs font-medium text-zinc-900 border-b-2 border-zinc-900">Chats</button>
      <button id="tabContacts" onclick="switchTab('contacts')" class="flex-1 py-2.5 text-xs font-medium text-zinc-500 border-b-2 border-transparent hover:text-zinc-700">Contacts</button>
    </div>
    <div class="flex-1 overflow-y-auto" id="leftPanel">
      <div class="p-4 text-center text-zinc-400 text-sm">Loading...</div>
    </div>
  </div>

  <!-- Right Panel: Chat Area -->
  <div class="flex-1 flex flex-col min-w-0">
    <!-- Chat Header -->
    <div class="h-16 border-b border-zinc-100 flex items-center px-6 gap-3 shrink-0" id="chatHeader">
      <div class="text-zinc-400 text-sm">Select a conversation to start chatting</div>
    </div>

    <!-- Messages Area -->
    <div class="flex-1 overflow-y-auto p-6 space-y-4" id="messagesArea">
      <div class="flex flex-col items-center justify-center h-full text-zinc-400">
        <i data-lucide="message-circle" class="h-12 w-12 mb-3 opacity-50"></i>
        <p class="text-sm">No conversation selected</p>
        <p class="text-xs mt-1">Choose a contact or existing chat to begin</p>
      </div>
    </div>

    <!-- Input Area -->
    <div class="border-t border-zinc-100 p-4 shrink-0 hidden" id="inputArea">
      <div class="flex gap-3">
        <input id="messageInput" type="text" placeholder="Type a message..." class="input flex-1" onkeydown="if(event.key==='Enter')sendMsg()">
        <button onclick="sendMsg()" class="btn btn-primary px-4">
          <i data-lucide="send" class="h-4 w-4"></i>
        </button>
      </div>
    </div>
  </div>
</div>

<script>
let currentTab = 'conversations';
let allContacts = [];
let allConversations = [];
let activeConversationId = null;
let activeReceiverId = null;
let pollInterval = null;

async function loadContacts() {
  try {
    allContacts = await messageApi.getContacts();
  } catch(e) { allContacts = []; }
}

async function loadConversations() {
  try {
    allConversations = await messageApi.getConversations();
  } catch(e) { allConversations = []; }
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

function filterContacts() {
  renderLeftPanel();
}

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
        '<div class="h-10 w-10 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600 shrink-0">' + initial + '</div>' +
        '<div class="flex-1 min-w-0">' +
          '<div class="flex items-center gap-2"><span class="font-medium text-sm text-zinc-900 truncate">' + escapeHtml(c.otherUserName) + '</span>' + roleBadge + '</div>' +
          '<p class="text-xs text-zinc-500 truncate mt-0.5">' + escapeHtml(c.lastMessage || '') + '</p>' +
        '</div>' +
        '<div class="flex flex-col items-end gap-1 shrink-0"><span class="text-xs text-zinc-400">' + time + '</span>' + unread + '</div>' +
      '</div>';
    }).join('');
  } else {
    let items = allContacts;
    if (query) items = items.filter(c => c.name.toLowerCase().includes(query));
    if (items.length === 0) {
      panel.innerHTML = '<div class="p-4 text-center text-zinc-400 text-sm">No contacts available</div>';
      return;
    }
    panel.innerHTML = items.map(c => {
      const initial = (c.name || 'U').charAt(0).toUpperCase();
      const roleBadge = getRoleBadge(c.role);
      return '<div onclick="startChat(\'' + c.id + '\',\'' + escapeAttr(c.name) + '\',\'' + c.role + '\')" class="flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-zinc-50 transition-colors">' +
        '<div class="h-10 w-10 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600 shrink-0">' + initial + '</div>' +
        '<div class="flex-1 min-w-0">' +
          '<div class="flex items-center gap-2"><span class="font-medium text-sm text-zinc-900 truncate">' + escapeHtml(c.name) + '</span>' + roleBadge + '</div>' +
          '<p class="text-xs text-zinc-500 mt-0.5">' + (c.department || '') + '</p>' +
        '</div>' +
      '</div>';
    }).join('');
  }
}

function getRoleBadge(role) {
  const colors = { admin: 'bg-purple-100 text-purple-700', organiser: 'bg-blue-100 text-blue-700', applicant: 'bg-emerald-100 text-emerald-700' };
  const cls = colors[role] || 'bg-zinc-100 text-zinc-600';
  return '<span class="text-xs px-1.5 py-0.5 rounded ' + cls + '">' + (role || '') + '</span>';
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
  setChatHeader(otherUserName, otherUserRole);
  document.getElementById('inputArea').classList.remove('hidden');
  loadMessages();
  startPolling();
  renderLeftPanel();
}

function startChat(userId, userName, userRole) {
  activeReceiverId = userId;
  // Build conversation ID deterministically
  const myId = getCurrentUserId();
  activeConversationId = myId < userId ? myId + '_' + userId : userId + '_' + myId;
  setChatHeader(userName, userRole);
  document.getElementById('inputArea').classList.remove('hidden');
  loadMessages();
  startPolling();
  switchTab('conversations');
}

function getCurrentUserId() {
  // Extract from session - we embed it in a meta tag
  return document.querySelector('meta[name="uid"]')?.content || '';
}

function setChatHeader(name, role) {
  const initial = (name || 'U').charAt(0).toUpperCase();
  const roleBadge = getRoleBadge(role);
  document.getElementById('chatHeader').innerHTML =
    '<div class="h-9 w-9 rounded-full bg-zinc-200 border border-zinc-300 flex items-center justify-center font-bold text-zinc-600">' + initial + '</div>' +
    '<div><div class="flex items-center gap-2"><span class="font-semibold text-zinc-900">' + escapeHtml(name) + '</span>' + roleBadge + '</div></div>';
  lucide.createIcons();
}

async function loadMessages() {
  if (!activeConversationId) return;
  try {
    const messages = await messageApi.getMessages(activeConversationId);
    renderMessages(messages);
    await messageApi.markRead(activeConversationId);
    loadConversations().then(renderLeftPanel);
  } catch(e) {
    document.getElementById('messagesArea').innerHTML = '<div class="text-center text-zinc-400 text-sm">Failed to load messages</div>';
  }
}

function renderMessages(messages) {
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
    const time = formatTime(m.timestamp);
    if (isMine) {
      html += '<div class="flex justify-end"><div class="max-w-[70%]"><div class="bg-zinc-900 text-white px-4 py-2.5 rounded-2xl rounded-br-md text-sm">' + escapeHtml(m.content) + '</div><div class="text-xs text-zinc-400 mt-1 text-right">' + time + '</div></div></div>';
    } else {
      html += '<div class="flex justify-start"><div class="max-w-[70%]"><div class="bg-zinc-100 text-zinc-900 px-4 py-2.5 rounded-2xl rounded-bl-md text-sm">' + escapeHtml(m.content) + '</div><div class="text-xs text-zinc-400 mt-1">' + time + '</div></div></div>';
    }
  });

  area.innerHTML = html;
  area.scrollTop = area.scrollHeight;
}

async function sendMsg() {
  const input = document.getElementById('messageInput');
  const content = input.value.trim();
  if (!content || !activeReceiverId) return;
  input.value = '';
  try {
    await messageApi.sendMessage(activeReceiverId, content);
    loadMessages();
  } catch(e) {
    showToast(e.message || 'Failed to send message', 'error');
  }
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

// Initial load
async function initMessaging() {
  await Promise.all([loadContacts(), loadConversations()]);
  renderLeftPanel();
  lucide.createIcons();
}
document.addEventListener('DOMContentLoaded', initMessaging);
</script>
