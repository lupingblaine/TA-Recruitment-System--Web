import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Search, Filter, MapPin, Clock, Briefcase, Sparkles } from 'lucide-react';
import { Link } from 'react-router';

export function ApplicantJobs() {
  const jobs = [
    {
      id: 1,
      title: 'Teaching Assistant - Data Structures',
      module: 'CS201',
      professor: 'Dr. Alan Turing',
      department: 'Computer Science',
      type: 'Part-time',
      hours: '10 hrs/week',
      deadline: 'Oct 15, 2026',
      tags: ['Java', 'Algorithms'],
      matchScore: 92,
    },
    {
      id: 2,
      title: 'Lab Assistant - Machine Learning',
      module: 'CS405',
      professor: 'Dr. Yann LeCun',
      department: 'Computer Science',
      type: 'Part-time',
      hours: '15 hrs/week',
      deadline: 'Oct 20, 2026',
      tags: ['Python', 'PyTorch'],
      matchScore: 85,
    },
    {
      id: 3,
      title: 'Head TA - Intro to Programming',
      module: 'CS101',
      professor: 'Dr. Grace Hopper',
      department: 'Computer Science',
      type: 'Full-time',
      hours: '20 hrs/week',
      deadline: 'Oct 25, 2026',
      tags: ['C++', 'Mentorship'],
      matchScore: 78,
    },
  ];

  return (
    <div className="space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">TA Positions</h1>
          <p className="text-zinc-500 mt-2">Find and apply for teaching assistant roles.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative w-full md:w-64">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
            <Input className="pl-9" placeholder="Search roles, modules..." />
          </div>
          <Button variant="outline" className="gap-2 shrink-0">
            <Filter className="h-4 w-4" />
            Filters
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {jobs.map((job, i) => (
          <motion.div
            key={job.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
          >
            <Card className="flex flex-col h-full hover:shadow-md transition-shadow dark:hover:border-zinc-700">
              <CardContent className="p-6 flex flex-col h-full">
                <div className="flex justify-between items-start mb-4">
                  <Badge variant="neutral">{job.module}</Badge>
                  <div className="flex items-center gap-2">
                    <Badge variant={job.matchScore >= 90 ? 'success' : job.matchScore >= 80 ? 'neutral' : 'warning'} className="gap-1 shadow-sm border border-zinc-200/50 dark:border-zinc-800">
                      <Sparkles className="h-3 w-3" />
                      {job.matchScore}% Match
                    </Badge>
                  </div>
                </div>
                
                <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-2 line-clamp-2">{job.title}</h3>
                <p className="text-sm text-zinc-600 dark:text-zinc-400 mb-4">{job.professor} • {job.department}</p>
                
                <div className="flex items-center gap-4 text-sm text-zinc-500 mb-6">
                  <div className="flex items-center gap-1.5">
                    <Clock className="h-4 w-4" />
                    {job.hours}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Briefcase className="h-4 w-4" />
                    {job.type}
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 mb-6">
                  {job.tags.map(tag => (
                    <Badge key={tag} variant="outline" className="bg-zinc-50 dark:bg-zinc-900 border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-300">
                      {tag}
                    </Badge>
                  ))}
                </div>

                <div className="mt-auto pt-4 border-t border-zinc-100 dark:border-zinc-800/60">
                  <Link to={`/applicant/jobs/${job.id}`}>
                    <Button className="w-full justify-between" variant="secondary">
                      View Details
                      <span className="text-zinc-400">&rarr;</span>
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
