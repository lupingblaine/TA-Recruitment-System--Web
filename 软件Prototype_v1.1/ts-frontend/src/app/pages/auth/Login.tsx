import React, { useState } from 'react';
import { motion } from 'motion/react';
import { useNavigate, Link } from 'react-router';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { User, Briefcase, LayoutDashboard } from 'lucide-react';

export function Login() {
  const navigate = useNavigate();
  const [role, setRole] = useState<'applicant' | 'organiser' | 'admin'>('applicant');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (role === 'applicant') navigate('/applicant');
    else if (role === 'organiser') navigate('/organiser');
    else navigate('/admin');
  };

  return (
    <div className="flex min-h-screen bg-zinc-50 dark:bg-zinc-950 items-center justify-center p-6 text-zinc-900 dark:text-zinc-50">
      <div className="absolute top-6 left-6 flex items-center gap-2 font-semibold text-lg">
        <Link to="/" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-zinc-900 dark:bg-white flex items-center justify-center shadow-sm">
            <span className="text-white dark:text-zinc-900 text-sm font-bold">TA</span>
          </div>
          <span>RecruitPro</span>
        </Link>
      </div>

      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-md rounded-2xl border border-zinc-200/60 bg-white p-8 shadow-xl dark:border-zinc-800/60 dark:bg-zinc-950"
      >
        <div className="mb-8">
          <h1 className="text-2xl font-bold tracking-tight mb-2">Welcome back</h1>
          <p className="text-zinc-500 text-sm dark:text-zinc-400">Sign in to your account to continue</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-4">
            <Input label="Email address" type="email" placeholder="name@university.edu" required />
            <Input label="Password" type="password" placeholder="••••••••" required />
          </div>

          <div className="space-y-3">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Sign in as</label>
            <div className="grid grid-cols-3 gap-3">
              <button
                type="button"
                onClick={() => setRole('applicant')}
                className={`flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all ${
                  role === 'applicant' 
                    ? 'border-zinc-900 bg-zinc-50 text-zinc-900 dark:border-white dark:bg-zinc-900 dark:text-white ring-1 ring-zinc-900 dark:ring-white' 
                    : 'border-zinc-200 bg-white text-zinc-500 hover:bg-zinc-50 dark:border-zinc-800 dark:bg-zinc-950 dark:hover:bg-zinc-900'
                }`}
              >
                <User className="h-5 w-5" />
                <span className="text-sm font-medium">Applicant</span>
              </button>
              <button
                type="button"
                onClick={() => setRole('organiser')}
                className={`flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all ${
                  role === 'organiser' 
                    ? 'border-zinc-900 bg-zinc-50 text-zinc-900 dark:border-white dark:bg-zinc-900 dark:text-white ring-1 ring-zinc-900 dark:ring-white' 
                    : 'border-zinc-200 bg-white text-zinc-500 hover:bg-zinc-50 dark:border-zinc-800 dark:bg-zinc-950 dark:hover:bg-zinc-900'
                }`}
              >
                <Briefcase className="h-5 w-5" />
                <span className="text-sm font-medium">Organiser</span>
              </button>
              <button
                type="button"
                onClick={() => setRole('admin')}
                className={`flex flex-col items-center justify-center gap-2 rounded-xl border p-4 transition-all ${
                  role === 'admin' 
                    ? 'border-zinc-900 bg-zinc-50 text-zinc-900 dark:border-white dark:bg-zinc-900 dark:text-white ring-1 ring-zinc-900 dark:ring-white' 
                    : 'border-zinc-200 bg-white text-zinc-500 hover:bg-zinc-50 dark:border-zinc-800 dark:bg-zinc-950 dark:hover:bg-zinc-900'
                }`}
              >
                <LayoutDashboard className="h-5 w-5" />
                <span className="text-sm font-medium">Admin</span>
              </button>
            </div>
          </div>

          <Button type="submit" className="w-full h-11 text-base">
            Sign In
          </Button>

          <p className="text-center text-sm text-zinc-500">
            Don't have an account? <Link to="/auth/signup" className="text-zinc-900 font-medium hover:underline dark:text-white">Sign up</Link>
          </p>
        </form>
      </motion.div>
    </div>
  );
}
