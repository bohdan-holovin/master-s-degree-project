import React, { useState, useEffect } from 'react';
import axios from 'axios';

const MonitoringComponent = () => {
    const [status, setStatus] = useState('');
    const [backupInfo, setBackupInfo] = useState([]);

    useEffect(() => {
        // Запит до REST API для отримання статусу
        axios.get('/api/monitoring/status')
            .then(response => {
                setStatus(response.data);
            })
            .catch(error => {
                console.error('Error fetching status:', error);
            });

        // Запит до REST API для отримання інформації про бекапи
        axios.get('/api/monitoring/backupInfo')
            .then(response => {
                setBackupInfo(response.data);
            })
            .catch(error => {
                console.error('Error fetching backup info:', error);
            });
    }, []);

    return (
        <div>
            <h2>Monitoring Dashboard</h2>
            <div>Status: {status}</div>
            <div>
                <h3>Backup Information</h3>
                <ul>
                    {backupInfo.map(info => (
                        <li key={info.id}>
                            {info.user} - {info.timestamp} - {info.status}
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default MonitoringComponent;
