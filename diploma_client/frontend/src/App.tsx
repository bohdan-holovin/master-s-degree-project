import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import {Web3App} from './Web3App';
import BackupFuncComponent from './BackupFuncComponent';

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/web3" element={<Web3App />} />
        <Route path="/monitoring" element={<BackupFuncComponent />} />
        <Route path="/" element={<Navigate to="/web3" />} />
      </Routes>
    </Router>
  );
};

export default App;
