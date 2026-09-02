import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage.jsx";
import RegisterPage from "./pages/RegisterPage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import StatisticsPage from "./pages/StatisticsPage.jsx";
import SearchPage from "./pages/SearchPage.jsx";
import { isLoggedIn } from "./auth.js";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/statistics" element={<StatisticsPage />} />
      <Route path="/search" element={<SearchPage />} />
      <Route
        path="/"
        element={<Navigate to={isLoggedIn() ? "/dashboard" : "/login"} />}
      />
    </Routes>
  );
}

export default App;