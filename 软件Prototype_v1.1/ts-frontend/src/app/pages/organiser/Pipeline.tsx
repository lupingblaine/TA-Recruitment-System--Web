import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { useNavigate } from 'react-router';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Search, Filter, MoreHorizontal, User, Calendar, MessageSquare, ChevronDown, X, FileText, Briefcase, GraduationCap, AlertTriangle, CheckCircle2, TrendingUp, Sparkles, BrainCircuit } from 'lucide-react';
import { DndProvider, useDrag, useDrop } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';

// Types
type Status = 'applied' | 'review' | 'shortlisted' | 'interview' | 'offer';

interface Candidate {
  id: string;
  name: string;
  role: string;
  status: Status;
  avatar: string;
  gpa: string;
  date: string;
  matchScore: number;
  riskFlag: 'overload' | 'missing_req' | null;
  aiSummary: string;
}

// Mock Data
const initialCandidates: Candidate[] = [
  { id: '1', name: 'Alex Johnson', role: 'CS201', status: 'review', avatar: 'A', gpa: '3.8', date: 'Oct 1', matchScore: 92, riskFlag: null, aiSummary: 'Strong algorithms background. Previously tutored CS101.' },
  { id: '2', name: 'Sarah Lee', role: 'CS405', status: 'shortlisted', avatar: 'S', gpa: '3.9', date: 'Oct 2', matchScore: 88, riskFlag: 'overload', aiSummary: 'Excellent academic record, but currently enrolled in 5 advanced modules.' },
  { id: '3', name: 'Michael Chen', role: 'CS101', status: 'applied', avatar: 'M', gpa: '3.5', date: 'Oct 3', matchScore: 75, riskFlag: null, aiSummary: 'Good communication skills, moderate technical fit.' },
  { id: '4', name: 'Emma Wilson', role: 'CS201', status: 'interview', avatar: 'E', gpa: '4.0', date: 'Sep 28', matchScore: 98, riskFlag: null, aiSummary: 'Perfect technical match. Extensive competitive programming experience.' },
  { id: '5', name: 'David Kim', role: 'CS405', status: 'offer', avatar: 'D', gpa: '3.7', date: 'Sep 25', matchScore: 85, riskFlag: 'missing_req', aiSummary: 'Strong ML project experience, missing formal prerequisite CS305.' },
  { id: '6', name: 'Lisa Wang', role: 'CS101', status: 'review', avatar: 'L', gpa: '3.6', date: 'Oct 1', matchScore: 81, riskFlag: null, aiSummary: 'Enthusiastic and clear communicator. Solid foundational knowledge.' },
];

const COLUMNS: { id: Status; title: string; color: string }[] = [
  { id: 'applied', title: 'Applied', color: 'bg-zinc-100 dark:bg-zinc-800' },
  { id: 'review', title: 'Review', color: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300' },
  { id: 'shortlisted', title: 'Shortlist', color: 'bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-300' },
  { id: 'interview', title: 'Interview', color: 'bg-amber-100 dark:bg-amber-900/30 text-amber-800 dark:text-amber-300' },
  { id: 'offer', title: 'Offer', color: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-800 dark:text-emerald-300' },
];

export function Pipeline() {
  const navigate = useNavigate();
  const [candidates, setCandidates] = useState<Candidate[]>(initialCandidates);
  const [filterModule, setFilterModule] = useState<string>('All');
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate | null>(null);

  const moveCandidate = (id: string, newStatus: Status) => {
    setCandidates((prev) =>
      prev.map((c) => (c.id === id ? { ...c, status: newStatus } : c))
    );
  };

  const filteredCandidates = candidates.filter(c => filterModule === 'All' || c.role === filterModule);

  return (
    <DndProvider backend={HTML5Backend}>
      <div className="space-y-6 h-full flex flex-col">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Recruitment Pipeline</h1>
            <p className="text-zinc-500 mt-2">Manage applicants through the hiring stages.</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="relative w-full md:w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
              <Input className="pl-9 bg-white" placeholder="Search candidates..." />
            </div>
            <select 
              className="h-10 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950 dark:border-zinc-800 dark:bg-zinc-950"
              value={filterModule}
              onChange={(e) => setFilterModule(e.target.value)}
            >
              <option value="All">All Modules</option>
              <option value="CS101">CS101</option>
              <option value="CS201">CS201</option>
              <option value="CS405">CS405</option>
            </select>
          </div>
        </div>

        <div className="flex-1 overflow-x-auto pb-4">
          <div className="flex gap-6 h-full min-w-max">
            {COLUMNS.map((col) => (
              <Column
                key={col.id}
                column={col}
                candidates={filteredCandidates.filter((c) => c.status === col.id)}
                moveCandidate={moveCandidate}
                onCandidateClick={setSelectedCandidate}
              />
            ))}
          </div>
        </div>
      </div>

      <AnimatePresence>
        {selectedCandidate && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-zinc-900/20 backdrop-blur-sm z-40 dark:bg-black/40"
              onClick={() => setSelectedCandidate(null)}
            />
            <motion.div
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 200 }}
              className="fixed inset-y-0 right-0 w-full md:w-[400px] bg-white dark:bg-zinc-950 border-l border-zinc-200 dark:border-zinc-800 shadow-2xl z-50 flex flex-col"
            >
              <div className="p-6 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between">
                <h2 className="text-lg font-bold text-zinc-900 dark:text-white">Applicant Summary</h2>
                <Button variant="ghost" size="icon" onClick={() => setSelectedCandidate(null)}>
                  <X className="h-5 w-5" />
                </Button>
              </div>
              
              <div className="flex-1 overflow-y-auto p-6 space-y-6">
                <div className="flex items-center gap-4">
                  <div className="h-16 w-16 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-xl font-bold text-zinc-700 dark:text-zinc-300 relative">
                    {selectedCandidate.avatar}
                    {selectedCandidate.riskFlag && (
                      <div className="absolute -top-1 -right-1 h-5 w-5 bg-red-500 rounded-full border-2 border-white dark:border-zinc-950 flex items-center justify-center">
                        <AlertTriangle className="h-3 w-3 text-white" />
                      </div>
                    )}
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-bold text-zinc-900 dark:text-white">{selectedCandidate.name}</h3>
                    <p className="text-sm text-zinc-500">Applied for {selectedCandidate.role}</p>
                    <Badge variant="neutral" className="mt-2 capitalize">{selectedCandidate.status}</Badge>
                  </div>
                </div>

                <div className="p-4 rounded-xl bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-indigo-950/30 dark:to-purple-950/30 border border-indigo-100 dark:border-indigo-900/30">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <Sparkles className="h-5 w-5 text-indigo-500" />
                      <h4 className="font-semibold text-indigo-900 dark:text-indigo-300">AI Match Analysis</h4>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-2xl font-bold text-indigo-700 dark:text-indigo-400">{selectedCandidate.matchScore}%</span>
                      <div className="text-xs font-medium px-2 py-1 bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 rounded-md">
                        {selectedCandidate.matchScore >= 90 ? 'Strong Fit' : selectedCandidate.matchScore >= 80 ? 'Good Fit' : 'Moderate Fit'}
                      </div>
                    </div>
                  </div>
                  <p className="text-sm text-indigo-800/80 dark:text-indigo-200/80 leading-relaxed mb-3">
                    {selectedCandidate.aiSummary}
                  </p>
                  
                  <div className="space-y-2 mt-4 pt-4 border-t border-indigo-200/50 dark:border-indigo-800/50">
                    <p className="text-xs font-semibold text-indigo-900/60 dark:text-indigo-300/60 uppercase tracking-wider">Why this score?</p>
                    <div className="grid grid-cols-1 gap-2 text-sm">
                      <div className="flex items-start gap-2">
                        <CheckCircle2 className="h-4 w-4 text-emerald-500 shrink-0 mt-0.5" />
                        <span className="text-indigo-900 dark:text-indigo-100">Strong academic performance in prerequisite modules.</span>
                      </div>
                      <div className="flex items-start gap-2">
                        <CheckCircle2 className="h-4 w-4 text-emerald-500 shrink-0 mt-0.5" />
                        <span className="text-indigo-900 dark:text-indigo-100">Previous teaching experience highly relevant.</span>
                      </div>
                      {selectedCandidate.riskFlag === 'overload' && (
                        <div className="flex items-start gap-2">
                          <AlertTriangle className="h-4 w-4 text-red-500 shrink-0 mt-0.5" />
                          <span className="text-red-700 dark:text-red-400 font-medium">Workload Warning: Currently assigned to maximum recommended hours.</span>
                        </div>
                      )}
                      {selectedCandidate.riskFlag === 'missing_req' && (
                        <div className="flex items-start gap-2">
                          <AlertTriangle className="h-4 w-4 text-amber-500 shrink-0 mt-0.5" />
                          <span className="text-amber-700 dark:text-amber-400 font-medium">Missing formal prerequisite, though offset by project experience.</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 rounded-xl bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800">
                    <p className="text-xs text-zinc-500 mb-1">Current GPA</p>
                    <p className="text-lg font-bold text-zinc-900 dark:text-white">{selectedCandidate.gpa}</p>
                  </div>
                  <div className="p-4 rounded-xl bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-100 dark:border-zinc-800">
                    <p className="text-xs text-zinc-500 mb-1">Applied On</p>
                    <p className="text-lg font-bold text-zinc-900 dark:text-white">{selectedCandidate.date}</p>
                  </div>
                </div>

                <div className="space-y-3">
                  <h4 className="text-sm font-semibold text-zinc-900 dark:text-white">Key Highlights</h4>
                  <div className="space-y-2">
                    <div className="flex items-start gap-3 text-sm">
                      <GraduationCap className="h-4 w-4 text-zinc-400 mt-0.5" />
                      <div>
                        <p className="font-medium text-zinc-900 dark:text-zinc-100">Year 3, Computer Science</p>
                        <p className="text-zinc-500">Expected graduation: 2027</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3 text-sm">
                      <Briefcase className="h-4 w-4 text-zinc-400 mt-0.5" />
                      <div>
                        <p className="font-medium text-zinc-900 dark:text-zinc-100">Previous TA Experience</p>
                        <p className="text-zinc-500">CS101 (Spring 2025)</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3 text-sm">
                      <FileText className="h-4 w-4 text-zinc-400 mt-0.5" />
                      <div>
                        <p className="font-medium text-zinc-900 dark:text-zinc-100">Grade in {selectedCandidate.role}</p>
                        <p className="text-zinc-500">A+ (Taken Fall 2024)</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="p-6 border-t border-zinc-200 dark:border-zinc-800 flex gap-3 bg-zinc-50 dark:bg-zinc-900/30">
                <Button 
                  className="flex-1" 
                  onClick={() => navigate(`/organiser/applications/${selectedCandidate.id}`)}
                >
                  View Full Application
                </Button>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </DndProvider>
  );
}

function Column({
  column,
  candidates,
  moveCandidate,
  onCandidateClick,
}: {
  column: { id: Status; title: string; color: string };
  candidates: Candidate[];
  moveCandidate: (id: string, newStatus: Status) => void;
  onCandidateClick: (candidate: Candidate) => void;
}) {
  const [{ isOver }, drop] = useDrop(() => ({
    accept: 'candidate',
    drop: (item: { id: string }) => moveCandidate(item.id, column.id),
    collect: (monitor) => ({
      isOver: !!monitor.isOver(),
    }),
  }));

  return (
    <div
      ref={drop}
      className={`flex flex-col w-80 shrink-0 bg-zinc-50/50 dark:bg-zinc-900/20 rounded-2xl p-4 border transition-colors ${
        isOver ? 'border-zinc-400 bg-zinc-100 dark:border-zinc-600 dark:bg-zinc-800/50' : 'border-transparent'
      }`}
    >
      <div className="flex items-center justify-between mb-4 px-2">
        <h3 className="font-semibold text-zinc-900 dark:text-white flex items-center gap-2">
          {column.title}
          <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${column.color}`}>
            {candidates.length}
          </span>
        </h3>
        <Button variant="ghost" size="icon" className="h-8 w-8 text-zinc-500">
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto">
        <AnimatePresence>
          {candidates.map((candidate) => (
            <CandidateCard 
              key={candidate.id} 
              candidate={candidate} 
              onClick={() => onCandidateClick(candidate)}
            />
          ))}
        </AnimatePresence>
      </div>
    </div>
  );
}

function CandidateCard({ candidate, onClick }: { candidate: Candidate, onClick: () => void }) {
  const [{ isDragging }, drag] = useDrag(() => ({
    type: 'candidate',
    item: { id: candidate.id },
    collect: (monitor) => ({
      isDragging: !!monitor.isDragging(),
    }),
  }));

  return (
    <motion.div
      ref={drag}
      layout
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.9 }}
      onClick={onClick}
      className={`bg-white dark:bg-zinc-950 p-4 rounded-xl border border-zinc-200/60 dark:border-zinc-800 shadow-sm hover:shadow-md transition-all cursor-grab active:cursor-grabbing ${
        isDragging ? 'opacity-50 scale-95' : 'opacity-100'
      }`}
    >
      <div className="flex justify-between items-start mb-3">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center font-bold text-zinc-700 dark:text-zinc-300 relative">
            {candidate.avatar}
            {candidate.riskFlag && (
              <div className="absolute -top-1 -right-1 h-4 w-4 bg-red-500 rounded-full border-2 border-white dark:border-zinc-950 flex items-center justify-center">
                <AlertTriangle className="h-2 w-2 text-white" />
              </div>
            )}
          </div>
          <div>
            <h4 className="font-semibold text-sm text-zinc-900 dark:text-white">{candidate.name}</h4>
            <p className="text-xs text-zinc-500">{candidate.role}</p>
          </div>
        </div>
        <div className="flex flex-col items-center justify-center">
          <div className="relative h-8 w-8 flex items-center justify-center">
            <svg className="w-8 h-8 transform -rotate-90">
              <circle cx="16" cy="16" r="14" stroke="currentColor" strokeWidth="3" fill="transparent" className="text-zinc-100 dark:text-zinc-800" />
              <circle 
                cx="16" cy="16" r="14" stroke="currentColor" strokeWidth="3" fill="transparent" 
                strokeDasharray={88} 
                strokeDashoffset={88 - (88 * candidate.matchScore) / 100}
                className={candidate.matchScore >= 90 ? 'text-indigo-500' : candidate.matchScore >= 80 ? 'text-emerald-500' : 'text-amber-500'} 
              />
            </svg>
            <span className="absolute text-[10px] font-bold text-zinc-700 dark:text-zinc-300">{candidate.matchScore}</span>
          </div>
        </div>
      </div>
      
      <div className="flex items-center justify-between text-xs text-zinc-500 mt-4 pt-4 border-t border-zinc-100 dark:border-zinc-800">
        <div className="flex items-center gap-1.5 font-medium">
          <span className="text-zinc-900 dark:text-white font-bold">GPA: {candidate.gpa}</span>
        </div>
        <div className="flex gap-2">
          {candidate.status === 'interview' && (
            <Calendar className="h-4 w-4 text-amber-500" />
          )}
          <MessageSquare className="h-4 w-4 text-zinc-400 hover:text-blue-500 cursor-pointer" />
        </div>
      </div>
    </motion.div>
  );
}
