import React, { useState } from 'react';
import { motion } from 'motion/react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Plus, Search, Filter, MoreHorizontal, BookOpen, Edit2, Trash2 } from 'lucide-react';

export function AdminModules() {
  const [searchQuery, setSearchQuery] = useState('');

  const modules = [
    { id: 1, code: 'CS101', name: 'Introduction to Programming', department: 'Computer Science', organiser: 'Dr. Alan Turing', term: 'Fall 2026', status: 'Active' },
    { id: 2, code: 'CS201', name: 'Data Structures and Algorithms', department: 'Computer Science', organiser: 'Dr. Grace Hopper', term: 'Fall 2026', status: 'Active' },
    { id: 3, code: 'CS405', name: 'Machine Learning', department: 'Computer Science', organiser: 'Dr. Geoffrey Hinton', term: 'Spring 2027', status: 'Draft' },
    { id: 4, code: 'EE200', name: 'Digital Logic Design', department: 'Electrical Eng.', organiser: 'Dr. Claude Shannon', term: 'Fall 2026', status: 'Active' },
    { id: 5, code: 'MTH100', name: 'Calculus I', department: 'Mathematics', organiser: 'Dr. Isaac Newton', term: 'Fall 2026', status: 'Inactive' },
  ];

  const filteredModules = modules.filter(m => 
    m.code.toLowerCase().includes(searchQuery.toLowerCase()) || 
    m.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    m.organiser.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Global Modules</h1>
          <p className="text-zinc-500 mt-2">Manage all courses and assign Module Organisers across the university.</p>
        </div>
        <Button className="gap-2 shrink-0 bg-zinc-900 hover:bg-zinc-800 text-white">
          <Plus className="h-4 w-4" /> Add Module
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
          <input 
            type="text" 
            placeholder="Search modules, names, or organisers..." 
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
                <th className="px-6 py-4 font-medium">Module</th>
                <th className="px-6 py-4 font-medium">Organiser (MO)</th>
                <th className="px-6 py-4 font-medium">Department</th>
                <th className="px-6 py-4 font-medium">Term</th>
                <th className="px-6 py-4 font-medium">Status</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {filteredModules.map((module, i) => (
                <motion.tr 
                  key={module.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.05 }}
                  className="hover:bg-zinc-50/50 dark:hover:bg-zinc-900/50 transition-colors group"
                >
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="h-8 w-8 rounded-md bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500">
                        <BookOpen className="h-4 w-4" />
                      </div>
                      <div>
                        <div className="font-semibold text-zinc-900 dark:text-white">{module.code}</div>
                        <div className="text-zinc-500 text-xs">{module.name}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-zinc-700 dark:text-zinc-300">
                    <div className="flex items-center gap-2">
                      <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center text-blue-700 dark:text-blue-400 font-bold text-xs">
                        {module.organiser.charAt(4)}
                      </div>
                      {module.organiser}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-zinc-600 dark:text-zinc-400">
                    {module.department}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-zinc-600 dark:text-zinc-400">
                    {module.term}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <Badge variant={
                      module.status === 'Active' ? 'success' : 
                      module.status === 'Draft' ? 'warning' : 'neutral'
                    }>
                      {module.status}
                    </Badge>
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
        {filteredModules.length === 0 && (
          <div className="p-8 text-center text-zinc-500">
            No modules found matching your search.
          </div>
        )}
      </Card>
    </div>
  );
}
