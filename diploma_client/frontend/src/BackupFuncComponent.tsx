// BackupFuncComponent.jsx
import React, {useState, useEffect} from 'react';
import axios from 'axios';
import Web3 from "web3";
import {useConnectWallet} from "@web3-onboard/react";

const BackupFuncComponent = () => {
    const [status, setStatus] = useState('');
    const [backupInfo, setBackupInfo] = useState([]);
    const [userId, setUserId] = useState('');
    const [sourceDirectory, setSourceDirectory] = useState('');
    const [backupDirectory, setBackupDirectory] = useState('');
    const [restoreDirectory, setRestoreDirectory] = useState('');

    const [{wallet}, connect] = useConnectWallet();
    const [web3, setWeb3] = useState<Web3 | null>(null);
    const [account, setAccount] = useState<string | null>(null);
    const [backupInterval, setBackupInterval] = useState(24);
    const [trueConst, setTrueConst] = useState(true);
    const [falseConst, setFalseConst] = useState(false);


    useEffect(() => {
        if (!wallet) connect();
    }, [wallet, connect]);

    useEffect(() => {
        if (wallet) setWeb3(new Web3(wallet.provider));
    }, [wallet]);

    useEffect(() => {
        if (web3) web3.eth.getAccounts().then((res: string[]) => setAccount(res[0]));
    }, [web3]);

    const handleCreateBackup = () => {
        axios.get(`http://localhost:8080/api/backup/create?sourceDirectory=${sourceDirectory}&userId=${account}`)
            .then(response => {
                console.log('Backup creation initiated:', response.data);
            })
            .catch(error => {
                console.error('Error creating backup:', error);
            });
    };

    const handleRestoreBackup = () => {
        axios.get(`http://localhost:8080/api/backup/restore?backupDirectory=${backupDirectory}&restoreDirectory=${restoreDirectory}&userId=${account}`)
            .then(response => {
                console.log('Backup restoration initiated:', response.data);
            })
            .catch(error => {
                console.error('Error restoring backup:', error);
            });
    };

    const handleDeleteBackup = () => {
        axios.delete(`http://localhost:8080/api/backup/delete?backupDirectory=${backupDirectory}&userId=${account}`)
            .then(response => {
                console.log('Backup deletion initiated:', response.data);
            })
            .catch(error => {
                console.error('Error deleting backup:', error);
            });
    };

    const handleRemoteCreateBackup = () => {
        axios.get(`http://localhost:8080/api/remote_backup/create?sourceDirectory=${sourceDirectory}&userId=${account}&isAws=${false}`)
            .then(response => {
                console.log('Backup creation initiated:', response.data);
            })
            .catch(error => {
                console.error('Error creating backup:', error);
            });
    };

    const handleRemoteRestoreBackup = () => {
        axios.get(`http://localhost:8080/api/remote_backup/restore?backupDirectory=${backupDirectory}&restoreDirectory=${restoreDirectory}&userId=${account}&isAws=${false}`)
            .then(response => {
                console.log('Backup restoration initiated:', response.data);
            })
            .catch(error => {
                console.error('Error restoring backup:', error);
            });
    };

    const handleRemoteDeleteBackup = () => {
        axios.delete(`http://localhost:8080/api/remote_backup/delete?backupDirectory=${backupDirectory}&userId=${account}&isAws=${false}`)
            .then(response => {
                console.log('Backup deletion initiated:', response.data);
            })
            .catch(error => {
                console.error('Error deleting backup:', error);
            });
    };

    const handleRemoteAwsCreateBackup = () => {
        axios.get(`http://localhost:8080/api/remote_backup/create?sourceDirectory=${sourceDirectory}&userId=${account}&isAws=${true}`)
            .then(response => {
                console.log('Backup creation initiated:', response.data);
            })
            .catch(error => {
                console.error('Error creating backup:', error);
            });
    };

    const handleRemoteAwsRestoreBackup = () => {
        axios.get(`http://localhost:8080/api/remote_backup/restore?backupDirectory=${backupDirectory}&restoreDirectory=${restoreDirectory}&userId=${account}&isAws=${true}`)
            .then(response => {
                console.log('Backup restoration initiated:', response.data);
            })
            .catch(error => {
                console.error('Error restoring backup:', error);
            });
    };

    const handleRemoteAwsDeleteBackup = () => {
        axios.delete(`http://localhost:8080/api/remote_backup/delete?backupDirectory=${backupDirectory}&userId=${account}&isAws=${true}`)
            .then(response => {
                console.log('Backup deletion initiated:', response.data);
            })
            .catch(error => {
                console.error('Error deleting backup:', error);
            });
    };

    const handleStatusBackup = () => {
        axios.get(`http://localhost:8080/api/monitoring/status?userId=${account}`)
            .then(response => {
                setStatus(response.data);
            })
            .catch(error => {
                console.error('Error fetching status:', error);
            });
    };

    const handleBackupInfo = () => {
        axios.get(`http://localhost:8080/api/monitoring/backupInfo?userId=${account}`)
            .then(response => {
                setBackupInfo(response.data);
            })
            .catch(error => {
                console.error('Error fetching backup info:', error);
            });
    };

    const handleScheduleBackup = () => {

        axios.post(`http://localhost:8080/api/backup/schedule?sourceDirectory=${sourceDirectory}&userId=${account}&interval=${backupInterval}`)
            .then(response => {
                console.log('Backup scheduled successfully:', response.data);
            })
            .catch(error => {
                console.error('Error scheduling backup:', error);
            });
    };

    const handleRemoteScheduleBackup = () => {

        axios.post(`http://localhost:8080/api/remote_backup/schedule?sourceDirectory=${sourceDirectory}&userId=${account}&interval=${backupInterval}&isAws=${false}`)
            .then(response => {
                console.log('Backup scheduled successfully:', response.data);
            })
            .catch(error => {
                console.error('Error scheduling backup:', error);
            });
    };

    const handleRemoteAwsScheduleBackup = () => {

        axios.post(`http://localhost:8080/api/remote_backup/schedule?sourceDirectory=${sourceDirectory}&userId=${account}&interval=${backupInterval}&isAws=${true}`)
            .then(response => {
                console.log('Backup scheduled successfully:', response.data);
            })
            .catch(error => {
                console.error('Error scheduling backup:', error);
            });
    };

    useEffect(() => {
        if (account) {
            // Викликати методи, які використовують account
            handleStatusBackup();
            handleBackupInfo();
        }
    }, [account]);


    return (
        <div>
            <h1>Monitoring Dashboard</h1>
            <div>Hello, {account}</div>
            <hr></hr>
            <div>Status: {status}</div>
            <hr></hr>
            <div>
                <h3>Backup Information</h3>
                <ul>
                    {backupInfo.map(info => (
                        <li key={info.id}>
                            {info.user} - {info.timestamp} - {info.status} - {info.storage}
                        </li>
                    ))}
                    {/*{backupInfo}*/}
                </ul>
            </div>
            <hr></hr>
            <h1>Local Backup</h1>
            <div>
                <h3>Create Backup</h3>
                <label>
                    Source Directory:
                    <input type="text" value={sourceDirectory} onChange={(e) => setSourceDirectory(e.target.value)}/>
                </label>
                <button onClick={handleCreateBackup}>Create Backup</button>
            </div>
            <hr></hr>
            <div>
                <h3>Restore Backup</h3>
                <label>
                    Backup Directory:
                    <input type="text" value={backupDirectory} onChange={(e) => setBackupDirectory(e.target.value)}/>
                </label>
                <label>
                    Restore Directory:
                    <input type="text" value={restoreDirectory} onChange={(e) => setRestoreDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRestoreBackup}>Restore Backup</button>
            </div>
            <hr/>
            <div>
                <h3>Delete Backup</h3>
                <label>
                    Backup Directory:
                    <input type="text" value={backupDirectory} onChange={(e) => setBackupDirectory(e.target.value)}/>
                </label>
                <button onClick={handleDeleteBackup}>Delete Backup</button>
            </div>
            <hr/>
            <div>
                <h3>Schedule Regular Backup</h3>
                <label>
                    Source Directory:
                    <input type="text" value={sourceDirectory} onChange={(e) => setSourceDirectory(e.target.value)}/>
                </label>
                <label>
                    Backup Interval (hours):
                    <input type="number" value={backupInterval} onChange={(e) => setBackupInterval(e.target.value)}/>
                </label>
                <button onClick={handleScheduleBackup}>Schedule Backup</button>
            </div>
            <hr></hr>


            <h1>Remote Server Backup</h1>
            <hr></hr>
            <div>
                <h3>Remote create Backup</h3>
                <label>
                    Source Directory:
                    <input type="text" value={sourceDirectory} onChange={(e) => setSourceDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRemoteCreateBackup}>Create Backup</button>
            </div>
            <hr></hr>
            <div>
                <h3>Remote restore Backup</h3>
                <label>
                    Backup Directory:
                    <input type="text" value={backupDirectory} onChange={(e) => setBackupDirectory(e.target.value)}/>
                </label>
                <label>
                    Restore Directory:
                    <input type="text" value={restoreDirectory} onChange={(e) => setRestoreDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRemoteRestoreBackup}>Restore Backup</button>
            </div>
            <hr/>
            <div>
                <h3>Remote delete Backup</h3>
                <label>
                    Backup Directory:
                    <input type="text" value={backupDirectory} onChange={(e) => setBackupDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRemoteDeleteBackup}>Delete Backup</button>
            </div>
            <hr/>


            <div>
                <h3>Schedule Regular Backup</h3>
                <label>
                    Source Directory:
                    <input type="text" value={sourceDirectory} onChange={(e) => setSourceDirectory(e.target.value)}/>
                </label>
                <label>
                    Backup Interval (hours):
                    <input type="number" value={backupInterval} onChange={(e) => setBackupInterval(e.target.value)}/>
                </label>
                <button onClick={handleRemoteScheduleBackup}>Schedule Backup</button>
            </div>

            <h1>Remote AWS Backup</h1>
            <hr></hr>
            <div>
                <h3>Remote AWS create Backup</h3>
                <label>
                    Source Directory:
                    <input type="text" value={sourceDirectory} onChange={(e) => setSourceDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRemoteAwsCreateBackup}>Create Backup</button>
            </div>
            <hr></hr>
            <div>
                <h3>Remote AWS restore Backup</h3>
                <label>
                    Backup Directory:
                    <input type="text" value={backupDirectory} onChange={(e) => setBackupDirectory(e.target.value)}/>
                </label>
                <label>
                    Restore Directory:
                    <input type="text" value={restoreDirectory} onChange={(e) => setRestoreDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRemoteAwsRestoreBackup}>Restore Backup</button>
            </div>
            <hr/>
            <div>
                <h3>Remote AWS delete Backup</h3>
                <label>
                    Backup Directory:
                    <input type="text" value={backupDirectory} onChange={(e) => setBackupDirectory(e.target.value)}/>
                </label>
                <button onClick={handleRemoteAwsDeleteBackup}>Delete Backup</button>
            </div>
            <hr/>


            <div>
                <h3>Schedule AWS Regular Backup</h3>
                <label>
                    Source Directory:
                    <input type="text" value={sourceDirectory} onChange={(e) => setSourceDirectory(e.target.value)}/>
                </label>
                <label>
                    Backup Interval (hours):
                    <input type="number" value={backupInterval} onChange={(e) => setBackupInterval(e.target.value)}/>
                </label>
                <button onClick={handleRemoteAwsScheduleBackup}>Schedule Backup</button>
            </div>

        </div>
    );
};

export default BackupFuncComponent;
