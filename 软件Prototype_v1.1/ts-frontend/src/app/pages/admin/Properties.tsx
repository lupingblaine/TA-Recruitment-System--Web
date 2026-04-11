import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Settings, Sliders, Calendar as CalendarIcon, Hash, MapPin, Plus, Trash2, Edit2, Shield } from 'lucide-react';

export function AdminProperties() {
  const sections = [
    {
      title: 'Global Terms',
      description: 'Manage academic terms and registration periods across the platform.',
      icon: CalendarIcon,
      items: [
        { name: 'Fall 2026', status: 'Active', details: 'Aug 15 - Dec 20' },
        { name: 'Spring 2027', status: 'Draft', details: 'Jan 10 - May 15' },
        { name: 'Summer 2027', status: 'Inactive', details: 'Jun 1 - Aug 10' }
      ]
    },
    {
      title: 'Departments & Faculties',
      description: 'Define organizational structures for module categorization.',
      icon: MapPin,
      items: [
        { name: 'Computer Science', status: 'Active', details: '14 Modules' },
        { name: 'Electrical Engineering', status: 'Active', details: '8 Modules' },
        { name: 'Mathematics', status: 'Active', details: '22 Modules' }
      ]
    },
    {
      title: 'Role Designations',
      description: 'System roles available for staff and applicants.',
      icon: Shield,
      items: [
        { name: 'Module Organiser (MO)', status: 'Active', details: 'Manage TA hiring for assigned modules' },
        { name: 'Adviser / Admin', status: 'Active', details: 'Platform-wide configuration and overview' },
        { name: 'Teaching Assistant', status: 'Active', details: 'Applicant role' }
      ]
    }
  ];

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Properties & Settings</h1>
          <p className="text-zinc-500 mt-2">Configure system-wide attributes and terminology.</p>
        </div>
        <Button className="gap-2 shrink-0">
          <Settings className="h-4 w-4" /> Advanced Settings
        </Button>
      </div>

      <div className="space-y-8">
        {sections.map((section, sIdx) => (
          <motion.div
            key={sIdx}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: sIdx * 0.1 }}
          >
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
                <div className="flex items-center gap-4">
                  <div className="h-10 w-10 rounded-full bg-zinc-100 dark:bg-zinc-800/50 flex items-center justify-center">
                    <section.icon className="h-5 w-5 text-zinc-600 dark:text-zinc-400" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">{section.title}</CardTitle>
                    <CardDescription>{section.description}</CardDescription>
                  </div>
                </div>
                <Button variant="outline" size="sm" className="gap-2 hidden sm:flex">
                  <Plus className="h-3.5 w-3.5" /> Add New
                </Button>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="divide-y divide-zinc-200 dark:divide-zinc-800 border-t border-zinc-200 dark:border-zinc-800">
                  {section.items.map((item, i) => (
                    <div key={i} className="flex items-center justify-between py-4 group hover:bg-zinc-50/50 dark:hover:bg-zinc-900/50 -mx-6 px-6 transition-colors">
                      <div className="flex items-center gap-4">
                        <div className="font-medium text-zinc-900 dark:text-white">{item.name}</div>
                        <Badge variant={
                          item.status === 'Active' ? 'success' : 
                          item.status === 'Draft' ? 'warning' : 'neutral'
                        }>
                          {item.status}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-6">
                        <span className="text-sm text-zinc-500 hidden sm:block">{item.details}</span>
                        <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                          <Button variant="ghost" size="icon" className="h-8 w-8 text-zinc-400 hover:text-zinc-900 dark:hover:text-white">
                            <Edit2 className="h-3.5 w-3.5" />
                          </Button>
                          <Button variant="ghost" size="icon" className="h-8 w-8 text-zinc-400 hover:text-red-500">
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
                <div className="mt-4 sm:hidden">
                  <Button variant="outline" size="sm" className="w-full gap-2">
                    <Plus className="h-3.5 w-3.5" /> Add New
                  </Button>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
