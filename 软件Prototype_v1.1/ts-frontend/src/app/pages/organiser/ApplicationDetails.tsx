import React, { useState } from 'react';
import { motion } from 'motion/react';
import { useParams, useNavigate } from 'react-router';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { ArrowLeft, Download, Mail, ExternalLink, Calendar, CheckCircle2, XCircle, Clock, FileText, Briefcase, GraduationCap, Star } from 'lucide-react';

export function OrganiserApplicationDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  // Mock application data
  const application = {
    id: id || '1',
    applicant: {
      name: 'Alex Johnson',
      email: 'alex.j@university.edu',
      avatar: 'A',
      year: 'Year 3',
      major: 'Computer Science',
      gpa: '3.8',
    },
    role: 'CS201',
    roleTitle: 'Data Structures and Algorithms',
    status: 'review',
    appliedDate: 'Oct 1, 2026',
    coverLetter: `Dear Hiring Committee,\n\nI am writing to express my strong interest in the Teaching Assistant position for CS201. Having taken the course last year and achieved an A+, I am deeply familiar with the curriculum and the common challenges students face when learning data structures.\n\nOver the past semester, I have been actively participating in coding study groups, helping peers debug their assignments and grasp complex algorithms like dynamic programming and graph traversals. My passion for mentoring, combined with my strong technical foundation, makes me an ideal candidate to support the CS201 teaching team.\n\nThank you for considering my application.`,
    history: [
      { id: 1, course: 'CS101', grade: 'A+', term: 'Fall 2024' },
      { id: 2, course: 'CS201', grade: 'A+', term: 'Spring 2025' },
      { id: 3, course: 'MTH100', grade: 'A', term: 'Fall 2024' },
    ],
    experience: [
      { id: 1, role: 'Peer Tutor', course: 'CS101', term: 'Fall 2025' },
      { id: 2, role: 'Software Engineering Intern', company: 'TechCorp', term: 'Summer 2026' }
    ]
  };

  return (
    <div className="space-y-8 max-w-6xl mx-auto pb-12">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/organiser/pipeline')} className="shrink-0">
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-2xl font-bold tracking-tight text-zinc-900 dark:text-white flex items-center gap-3">
            {application.applicant.name}'s Application
            <Badge variant="neutral" className="capitalize">{application.status}</Badge>
          </h1>
          <p className="text-zinc-500 mt-1">Applying for {application.role} - {application.roleTitle}</p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <Button variant="outline" className="gap-2">
            <Mail className="h-4 w-4" /> Message
          </Button>
          <Button className="gap-2 bg-zinc-900 hover:bg-zinc-800 text-white">
            <Calendar className="h-4 w-4" /> Schedule Interview
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <Card className="p-6">
            <h2 className="text-lg font-bold text-zinc-900 dark:text-white mb-4 flex items-center gap-2">
              <FileText className="h-5 w-5 text-zinc-400" />
              Cover Letter
            </h2>
            <div className="text-sm text-zinc-600 dark:text-zinc-400 whitespace-pre-wrap leading-relaxed">
              {application.coverLetter}
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-zinc-900 dark:text-white flex items-center gap-2">
                <FileText className="h-5 w-5 text-zinc-400" />
                Resume / CV
              </h2>
              <Button variant="outline" size="sm" className="gap-2">
                <Download className="h-4 w-4" /> Download PDF
              </Button>
            </div>
            
            <div className="h-64 border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-xl flex flex-col items-center justify-center bg-zinc-50 dark:bg-zinc-900/20 text-zinc-500">
              <FileText className="h-10 w-10 text-zinc-300 dark:text-zinc-700 mb-3" />
              <p className="font-medium text-zinc-700 dark:text-zinc-300">resume_alex_johnson.pdf</p>
              <p className="text-xs">Preview not available in browser. Please download to view.</p>
            </div>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="p-6">
            <h3 className="font-bold text-zinc-900 dark:text-white mb-4">Applicant Profile</h3>
            
            <div className="flex items-center gap-4 mb-6">
              <div className="h-14 w-14 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-xl font-bold text-zinc-700 dark:text-zinc-300">
                {application.applicant.avatar}
              </div>
              <div>
                <p className="font-semibold text-zinc-900 dark:text-white">{application.applicant.name}</p>
                <p className="text-sm text-zinc-500">{application.applicant.email}</p>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-zinc-100 dark:border-zinc-800">
              <div className="flex justify-between text-sm">
                <span className="text-zinc-500 flex items-center gap-2"><GraduationCap className="h-4 w-4" /> Major</span>
                <span className="font-medium text-zinc-900 dark:text-white">{application.applicant.major}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-zinc-500 flex items-center gap-2"><Clock className="h-4 w-4" /> Year</span>
                <span className="font-medium text-zinc-900 dark:text-white">{application.applicant.year}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-zinc-500 flex items-center gap-2"><Star className="h-4 w-4" /> CGPA</span>
                <span className="font-medium text-zinc-900 dark:text-white">{application.applicant.gpa}</span>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="font-bold text-zinc-900 dark:text-white mb-4">Academic History</h3>
            <div className="space-y-3">
              {application.history.map((course) => (
                <div key={course.id} className="flex items-center justify-between p-3 rounded-lg bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800">
                  <div>
                    <p className="text-sm font-semibold text-zinc-900 dark:text-white">{course.course}</p>
                    <p className="text-xs text-zinc-500">{course.term}</p>
                  </div>
                  <Badge variant={course.grade.includes('A') ? 'success' : 'neutral'}>{course.grade}</Badge>
                </div>
              ))}
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="font-bold text-zinc-900 dark:text-white mb-4">Application Actions</h3>
            <div className="space-y-3">
              <Button className="w-full justify-start gap-2 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-900/50 border" variant="ghost">
                <CheckCircle2 className="h-4 w-4" /> Shortlist Candidate
              </Button>
              <Button className="w-full justify-start gap-2 bg-red-50 text-red-700 hover:bg-red-100 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-900/50 border" variant="ghost">
                <XCircle className="h-4 w-4" /> Reject Application
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
