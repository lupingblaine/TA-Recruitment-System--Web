import React, { useState } from 'react';
import { motion } from 'motion/react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Search, Filter, MoreHorizontal, User, Shield, Briefcase, Mail, Edit2, Trash2, Plus } from 'lucide-react';

export function AdminUsers() {
  const [searchQuery, setSearchQuery] = useState('');

  const users = [
    { id: 1, name: 'Alan Turing', email: 'alan@university.edu', role: 'Organiser', department: 'Computer Science', status: 'Active', lastLogin: '2 mins ago' },
    { id: 2, name: 'Emma Watson', email: 'emma@university.edu', role: 'Applicant', department: 'Arts & Humanities', status: 'Active', lastLogin: '1 hour ago' },
    { id: 3, name: 'John von Neumann', email: 'john@university.edu', role: 'Admin', department: 'System', status: 'Active', lastLogin: '4 hours ago' },
    { id: 4, name: 'David Kim', email: 'david@university.edu', role: 'Applicant', department: 'Engineering', status: 'Suspended', lastLogin: '2 days ago' },
    { id: 5, name: 'Grace Hopper', email: 'grace@university.edu', role: 'Organiser', department: 'Computer Science', status: 'Active', lastLogin: '1 week ago' },
  ];

  const filteredUsers = users.filter(u => 
    u.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    u.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'Admin': return <Shield className="h-4 w-4 text-purple-500" />;
      case 'Organiser': return <Briefcase className="h-4 w-4 text-blue-500" />;
      case 'Applicant': return <User className="h-4 w-4 text-emerald-500" />;
      default: return <User className="h-4 w-4 text-zinc-500" />;
    }
  };

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">User Management</h1>
          <p className="text-zinc-500 mt-2">Manage accounts, roles, and access permissions across the platform.</p>
        </div>
        <Button className="gap-2 shrink-0 bg-zinc-900 hover:bg-zinc-800 text-white">
          <Plus className="h-4 w-4" /> Invite User
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
          <input 
            type="text" 
            placeholder="Search by name or email..." 
            className="w-full pl-10 pr-4 py-2 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900/10 dark:focus:ring-white/10"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <Button variant="outline" className="gap-2 shrink-0 w-full sm:w-auto">
          <Filter className="h-4 w-4" /> Filters
        </Button>
      </div>

      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-zinc-500 bg-zinc-50/50 dark:bg-zinc-900/50 border-b border-zinc-200 dark:border-zinc-800 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4 font-medium">User Details</th>
                <th className="px-6 py-4 font-medium">Role</th>
                <th className="px-6 py-4 font-medium">Department</th>
                <th className="px-6 py-4 font-medium">Status</th>
                <th className="px-6 py-4 font-medium">Last Login</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {filteredUsers.map((user, i) => (
                <motion.tr 
                  key={user.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.05 }}
                  className="hover:bg-zinc-50/50 dark:hover:bg-zinc-900/50 transition-colors group"
                >
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="h-9 w-9 rounded-full bg-zinc-100 dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700 flex items-center justify-center font-bold text-zinc-600 dark:text-zinc-300">
                        {user.name.charAt(0)}
                      </div>
                      <div>
                        <div className="font-semibold text-zinc-900 dark:text-white">{user.name}</div>
                        <div className="text-zinc-500 text-xs flex items-center gap-1 mt-0.5">
                          <Mail className="h-3 w-3" /> {user.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2 text-zinc-700 dark:text-zinc-300">
                      {getRoleIcon(user.role)}
                      <span>{user.role}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-zinc-600 dark:text-zinc-400">
                    {user.department}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <Badge variant={user.status === 'Active' ? 'success' : 'neutral'}>
                      {user.status}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-zinc-500 text-xs">
                    {user.lastLogin}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <Button variant="ghost" size="icon" className="h-8 w-8 text-zinc-500 hover:text-zinc-900 dark:hover:text-white">
                        <Edit2 className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" className="h-8 w-8 text-zinc-500 hover:text-red-600 dark:hover:text-red-400">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>
        {filteredUsers.length === 0 && (
          <div className="p-8 text-center text-zinc-500">
            No users found matching your search.
          </div>
        )}
      </Card>
    </div>
  );
}
