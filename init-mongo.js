// MongoDB initialization script for test database
// noinspection JSUnresolvedReference

db = db.getSiblingDB('link-spray-test');

// Create collections that your application uses
db.createCollection('linkItem');
db.createCollection('abuseReport');
db.createCollection('dashboardEntity');
db.createCollection('mtLinkSprayCollectionItem');

// Create indexes for better performance
db.linkItem.createIndex({"id": 1}, {unique: true});
db.linkItem.createIndex({"createdAt": 1});
db.abuseReport.createIndex({"createdAt": 1});

print('Test database initialized successfully');