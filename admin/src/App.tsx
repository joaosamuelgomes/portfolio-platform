import type { ReactNode } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { ArticleEditPage } from './articles/ArticleEditPage';
import { ArticlesPage } from './articles/ArticlesPage';
import { LoginPage } from './auth/LoginPage';
import { useAuth } from './auth/session';
import { ProjectEditPage } from './projects/ProjectEditPage';
import { ProjectsPage } from './projects/ProjectsPage';
import { Layout } from './ui/Layout';

function RequireSession({ children }: { children: ReactNode }) {
  const { session } = useAuth();
  const location = useLocation();
  if (!session) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return children;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireSession>
            <Layout />
          </RequireSession>
        }
      >
        <Route index element={<Navigate to="/projects" replace />} />
        <Route path="projects" element={<ProjectsPage />} />
        <Route path="projects/new" element={<ProjectEditPage />} />
        <Route path="projects/:id" element={<ProjectEditPage />} />
        <Route path="articles" element={<ArticlesPage />} />
        <Route path="articles/new" element={<ArticleEditPage />} />
        <Route path="articles/:id" element={<ArticleEditPage />} />
        <Route path="*" element={<Navigate to="/projects" replace />} />
      </Route>
    </Routes>
  );
}
