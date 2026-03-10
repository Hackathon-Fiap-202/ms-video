db = db.getSiblingDB('order-db');

db.createUser({
    user: 'order_user',
    pwd: 'order_pass',
    roles: [
        { role: 'readWrite', db: 'order-db' },
        { role: 'clusterMonitor', db: 'admin' }
    ]
});