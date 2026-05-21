<%@ page pageEncoding="UTF-8" %>

<div class="flex h-[calc(100vh-8rem)] max-w-6xl mx-auto gap-0 rounded-2xl overflow-hidden border border-zinc-200 bg-white shadow-sm">
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

  <div class="flex-1 flex flex-col min-w-0">
    <div class="h-16 border-b border-zinc-100 flex items-center px-6 gap-3 shrink-0" id="chatHeader">
      <div class="text-zinc-400 text-sm">Select a conversation to start chatting</div>
    </div>

    <div class="flex-1 overflow-y-auto p-6 space-y-4" id="messagesArea">
      <div class="flex flex-col items-center justify-center h-full text-zinc-400">
        <i data-lucide="message-circle" class="h-12 w-12 mb-3 opacity-50"></i>
        <p class="text-sm">No conversation selected</p>
        <p class="text-xs mt-1">Choose a contact or existing chat to begin</p>
      </div>
    </div>

    <div class="border-t border-zinc-100 shrink-0 hidden" id="inputArea">
      <div id="replyBar" class="hidden px-4 pt-3 pb-1 border-b border-zinc-100 bg-zinc-50">
        <div class="flex items-start justify-between gap-2">
          <div class="flex-1 min-w-0 border-l-2 border-zinc-400 pl-3">
            <p class="text-xs font-medium text-zinc-600" id="replyBarTitle">Reply</p>
            <p class="text-xs text-zinc-500 truncate" id="replyBarPreview"></p>
          </div>
          <button type="button" onclick="cancelReply()" class="p-1 text-zinc-400 hover:text-zinc-700"><i data-lucide="x" class="h-4 w-4"></i></button>
        </div>
      </div>
      <div id="editBar" class="hidden px-4 pt-3 pb-1 border-b border-amber-100 bg-amber-50">
        <div class="flex items-center justify-between gap-2">
          <p class="text-xs font-medium text-amber-800">Editing message</p>
          <button type="button" onclick="cancelEdit()" class="text-xs text-amber-700 hover:underline">Cancel</button>
        </div>
      </div>
      <div class="p-4">
        <div class="flex gap-2 items-end">
          <input type="file" id="fileInput" class="hidden" accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip" onchange="onFileSelected(event)">
          <button type="button" onclick="document.getElementById('fileInput').click()" class="btn btn-secondary px-3 shrink-0" title="Attach file or image">
            <i data-lucide="paperclip" class="h-4 w-4"></i>
          </button>
          <textarea id="messageInput" rows="1" placeholder="Type a message... (Shift+Enter for new line)" class="input flex-1 min-h-[42px] max-h-32 resize-y"></textarea>
          <button type="button" id="sendBtn" onclick="sendMsg()" class="btn btn-primary px-4 shrink-0">
            <i data-lucide="send" class="h-4 w-4"></i>
          </button>
        </div>
        <p class="text-xs text-zinc-400 mt-2">Enter to send · Shift+Enter for new line · Recall within 2 min · Edit within 15 min</p>
      </div>
    </div>
  </div>
</div>

<!-- User Profile Card Modal -->
<div id="userCardModal" class="fixed inset-0 z-[60] hidden">
  <div class="absolute inset-0 bg-zinc-900/50 backdrop-blur-sm" onclick="closeUserCard()"></div>
  <div class="absolute inset-0 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden">
      <div class="relative bg-gradient-to-br from-zinc-800 to-zinc-900 p-6 pb-12 text-center">
        <button onclick="closeUserCard()" class="absolute top-3 right-3 text-white/60 hover:text-white"><i data-lucide="x" class="h-5 w-5"></i></button>
        <div class="h-16 w-16 rounded-full bg-white/20 border-2 border-white/40 flex items-center justify-center text-2xl font-bold text-white mx-auto" id="ucAvatar">?</div>
        <h3 class="text-lg font-bold text-white mt-3" id="ucName">-</h3>
        <div class="mt-1" id="ucRole"></div>
      </div>
      <div class="px-6 pt-4 pb-6 space-y-4 max-h-80 overflow-y-auto">
        <div class="grid grid-cols-2 gap-3 text-sm" id="ucBasic"></div>
        <div id="ucExtra"></div>
      </div>
      <div class="px-6 pb-4">
        <button onclick="closeUserCard()" class="btn btn-secondary w-full">Close</button>
      </div>
    </div>
  </div>
</div>

<style>
.msg-bubble { width: fit-content; max-width: 100%; }
.msg-bubble p { margin: 0; }
</style>
