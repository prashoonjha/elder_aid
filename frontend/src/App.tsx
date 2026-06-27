import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { WelcomePage } from './pages/WelcomePage';
import { ForWhomPage } from './pages/ForWhomPage';
import { RegisterPage } from './pages/RegisterPage';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { ElderlyProfileFormPage } from './pages/ElderlyProfileFormPage';
import { TaskFormPage } from './pages/TaskFormPage';
import { TaskBrowsePage } from './pages/TaskBrowsePage';
import { TaskDetailPage } from './pages/TaskDetailPage';
import { MyTasksPage } from './pages/MyTasksPage';
import { TaskApplicationsReviewPage } from './pages/TaskApplicationsReviewPage';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<WelcomePage />} />
          <Route path="/register/for-whom" element={<ForWhomPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/profiles/new" element={<ElderlyProfileFormPage />} />
            <Route path="/tasks/new" element={<TaskFormPage />} />
            <Route path="/tasks/mine" element={<MyTasksPage />} />
            <Route path="/tasks/mine/:taskId/review" element={<TaskApplicationsReviewPage />} />
            <Route path="/tasks" element={<TaskBrowsePage />} />
            <Route path="/tasks/:taskId" element={<TaskDetailPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
