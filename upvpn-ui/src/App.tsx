import { HashRouter, Route, Routes } from "react-router-dom";
import SignIn from "./pages/SignIn";
import DaemonOffline from "./pages/DaemonOffline";
import { Toaster } from "react-hot-toast";
import Home from "./pages/Home";
import ProtectedRoute from "./components/ProtectedRoute";
import Locations from "./pages/Locations";
import { LocationProvider } from "./context/LocationContext";
import { VpnStatusProvider } from "./context/VpnStatusContext";
import Settings from "./pages/Settings";
import Help from "./pages/Help";
import Plan from "./pages/Plan";
import Refer from "./pages/Refer";
import { NotificationProvider } from "./context/NotificationContext";
import NotificationDialog from "./components/NotificationDialog";

function App() {
  return (
    <HashRouter>
      <LocationProvider>
        <VpnStatusProvider>
          <NotificationProvider>
            <Routes>
              <Route element={<ProtectedRoute />}>
                <Route path="/" element={<Home />} />
                <Route path="/locations" element={<Locations />} />
                <Route path="/settings" element={<Settings />} />
                <Route path="/help" element={<Help />} />
                <Route path="/plan" element={<Plan />} />
                <Route path="/refer" element={<Refer />} />
              </Route>
              <Route path="/daemon-offline" element={<DaemonOffline />} />
              <Route path="/sign-in" element={<SignIn />} />
            </Routes>
            <NotificationDialog />
          </NotificationProvider>
        </VpnStatusProvider>
      </LocationProvider>
      <Toaster />
    </HashRouter>
  );
}

export default App;
