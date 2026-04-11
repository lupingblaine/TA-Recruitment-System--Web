import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router';
import { motion } from 'motion/react';
import { LayoutDashboard, Users, Briefcase, Calendar, Bell, Settings, User, LogOut, ChevronRight, BookOpen, Sliders, TrendingUp } from 'lucide-react';
import { cn } from './ui/Button';

const getNavItems = (pathname: string) => {
  if (pathname.startsWith('/applicant')) {
    return [
      { name: 'Dashboard', href: '/applicant', icon: LayoutDashboard },
      { name: 'My Applications', href: '/applicant/applications', icon: Briefcase },
      { name: 'Find Jobs', href: '/applicant/jobs', icon: Users },
      { name: 'Profile', href: '/applicant/profile', icon: User },
    ];
  } else if (pathname.startsWith('/organiser')) {
    return [
      { name: 'Overview', href: '/organiser', icon: LayoutDashboard },
      { name: 'TA Positions', href: '/organiser/jobs', icon: Briefcase },
      { name: 'Pipeline', href: '/organiser/pipeline', icon: Users },
      { name: 'Interviews', href: '/organiser/interviews', icon: Calendar },
    ];
  } else if (pathname.startsWith('/admin')) {
    return [
      { name: 'Analytics', href: '/admin', icon: LayoutDashboard },
      { name: 'Workload Intel', href: '/admin/workload', icon: TrendingUp },
      { name: 'Global Modules', href: '/admin/modules', icon: BookOpen },
      { name: 'Properties', href: '/admin/properties', icon: Sliders },
      { name: 'Users', href: '/admin/users', icon: Users },
      { name: 'System Logs', href: '/admin/logs', icon: Settings },
    ];
  }
  return [];
};

export function Layout() {
  const location = useLocation();
  const navItems = getNavItems(location.pathname);
  const isDark = false; // Add theme toggle later

  return (
    <div className="flex h-screen w-full bg-[#FAFAFA] text-zinc-900 dark:bg-zinc-950 dark:text-zinc-50 font-sans selection:bg-zinc-200">
      <aside className="hidden md:flex w-64 flex-col border-r border-zinc-200/50 bg-white/50 backdrop-blur-xl dark:border-zinc-800/50 dark:bg-zinc-950/50">
        <div className="flex h-14 items-center px-6 pt-2">
          <div className="flex items-center gap-2 font-semibold">
            <div className="h-6 w-6 rounded-md bg-zinc-900 dark:bg-zinc-50 flex items-center justify-center">
              <span className="text-white dark:text-zinc-900 text-xs font-bold">TA</span>
            </div>
            <span>RecruitPro</span>
          </div>
        </div>
        
        <div className="flex-1 overflow-auto py-6">
          <nav className="space-y-1 px-4">
            {navItems.map((item) => {
              const isActive = location.pathname === item.href;
              return (
                <NavLink
                  key={item.name}
                  to={item.href}
                  className={cn(
                    "group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-all duration-200 relative",
                    isActive
                      ? "text-zinc-900 dark:text-white"
                      : "text-zinc-500 hover:text-zinc-900 hover:bg-zinc-100/50 dark:text-zinc-400 dark:hover:text-white dark:hover:bg-zinc-800/50"
                  )}
                >
                  {isActive && (
                    <motion.div
                      layoutId="sidebar-active"
                      className="absolute inset-0 rounded-lg bg-zinc-100 dark:bg-zinc-800/50"
                      initial={false}
                      transition={{ type: "spring", stiffness: 300, damping: 30 }}
                    />
                  )}
                  <item.icon className="h-4 w-4 relative z-10" />
                  <span className="relative z-10">{item.name}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        <div className="p-4 mt-auto border-t border-zinc-200/50 dark:border-zinc-800/50">
          <button className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-zinc-500 hover:bg-zinc-100/50 hover:text-zinc-900 transition-colors">
            <LogOut className="h-4 w-4" />
            <span>Sign out</span>
          </button>
        </div>
      </aside>

      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <header className="h-14 flex items-center justify-between px-6 border-b border-zinc-200/50 bg-white/30 backdrop-blur-md dark:border-zinc-800/50 dark:bg-zinc-950/30 sticky top-0 z-10">
          <div className="flex items-center gap-2 text-sm text-zinc-500">
             {/* Breadcrumb style */}
             <span>Platform</span>
             <ChevronRight className="h-4 w-4" />
             <span className="text-zinc-900 font-medium capitalize dark:text-white">
               {location.pathname.split('/')[1]}
             </span>
          </div>
          <div className="flex items-center gap-4">
            <button className="relative p-2 text-zinc-400 hover:text-zinc-900 transition-colors rounded-full hover:bg-zinc-100">
              <Bell className="h-5 w-5" />
              <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-blue-500"></span>
            </button>
            <div className="h-8 w-8 rounded-full bg-zinc-200 dark:bg-zinc-800 border border-zinc-300 dark:border-zinc-700 overflow-hidden">
               <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=64&h=64" alt="Avatar" className="h-full w-full object-cover" />
            </div>
          </div>
        </header>
        
        <div className="flex-1 overflow-auto p-6 md:p-8">
          <div className="max-w-6xl mx-auto w-full">
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
}
