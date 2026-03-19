CREATE TABLE uac001 (
    syskey BIGINT NOT NULL DEFAULT 0,
    autokey BIGINT IDENTITY(1,1) NOT NULL,
    createddate VARCHAR(50) NOT NULL DEFAULT '',
    modifieddate VARCHAR(50) NOT NULL DEFAULT '',
    createduser VARCHAR(50) DEFAULT '',
    modifieduser VARCHAR(50) DEFAULT '',
    recordstatus INT DEFAULT 0,
    t1 VARCHAR(50) DEFAULT '',
    t2 VARCHAR(50) DEFAULT '',
    uuid VARCHAR(50) DEFAULT '',
    t3 VARCHAR(255) DEFAULT '',
    t4 VARCHAR(255) DEFAULT '',
    PRIMARY KEY (syskey),
    INDEX idx_uvm001_createddate (createddate),
    INDEX idx_uvm001_recordstatus (recordstatus),
);

INSERT INTO uac001 (syskey, createddate, modifieddate, createduser, modifieduser, recordstatus, t1, t2, uuid, t3, t4) VALUES 
(1, '2025-11-07 11:05:25 PM', '2025-11-07 11:05:25 PM', 'sa', 'sa', 1, 'genius.iq', 'Soe Htet Paing', 'JzzwTvbUugP', 'eeYZZ1RrhePe8acHMxIp/w==.iDwBi2tXaDr3gOF3zwlr7A==', 'eeYZZ1RrhePe8acHMxIp/w==.iDwBi2tXaDr3gOF3zwlr7A=='),
(251107110530122, '2025-11-07 11:05:30 PM', '2025-11-07 11:05:30 PM', 'genius.iq', 'genius.iq', 1, 'demo', 'Demo User', 'fMfbCxtFvBW', 'Wg4fpkMPOORdhUKkhnUu2g==.1yiv3nZ7K/ZipPyMTgS60w==', 'eeYZZ1RrhePe8acHMxIp/w==.iDwBi2tXaDr3gOF3zwlr7A==');

CREATE TABLE uac002 (
    syskey BIGINT NOT NULL DEFAULT 0,
    autokey BIGINT IDENTITY(1,1) NOT NULL,
    createddate VARCHAR(50) NOT NULL DEFAULT '',
    modifieddate VARCHAR(50) NOT NULL DEFAULT '',
    createduser VARCHAR(50) DEFAULT '',
    modifieduser VARCHAR(50) DEFAULT '',
    recordstatus INT DEFAULT 0,
    t1 VARCHAR(50) DEFAULT '',
    t2 VARCHAR(50) DEFAULT '',
    PRIMARY KEY (syskey),
);

INSERT INTO uac002 (syskey, createddate, modifieddate, createduser, modifieduser, recordstatus, t1, t2) VALUES 
(251108125804934, '2025-11-08 12:58:21 AM', '2025-11-08 12:58:21 AM', 'genius.iq', 'genius.iq', 1, 'admin', 'admin'),
(251108125821837, '2025-11-08 12:58:21 AM', '2025-11-08 12:58:21 AM', 'genius.iq', 'genius.iq', 1, 'test', 'test');

CREATE TABLE jun001 (
    syskey BIGINT NOT NULL DEFAULT 0,
    autokey BIGINT IDENTITY(1,1) NOT NULL,
    createddate VARCHAR(50) NOT NULL DEFAULT '',
    modifieddate VARCHAR(50) NOT NULL DEFAULT '',
    createduser VARCHAR(50) DEFAULT '',
    modifieduser VARCHAR(50) DEFAULT '',
    recordstatus INT DEFAULT 0,
    n1 BIGINT DEFAULT 0,
    n2 BIGINT DEFAULT 0,
    PRIMARY KEY (syskey),
);

INSERT INTO jun001 (syskey, createddate, modifieddate, createduser, modifieduser, recordstatus, n1, n2) VALUES 
(251108010206373, '2025-11-08 01:02:06 AM', '2025-11-08 01:02:06 AM', 'genius.iq', 'genius.iq', 1, 1, 251108125804934),
(251108010212493, '2025-11-08 01:02:12 AM', '2025-11-08 01:02:12 AM', 'genius.iq', 'genius.iq', 1, 251107110530122, 251108125821837);

CREATE TABLE vac001 (
    syskey BIGINT NOT NULL DEFAULT 0,
    autokey BIGINT IDENTITY(1,1) NOT NULL,
    createddate VARCHAR(50) NOT NULL DEFAULT '',
    createduser VARCHAR(50) DEFAULT '',
    t1 VARCHAR(50) NOT NULL DEFAULT '',
    PRIMARY KEY (syskey),
);

CREATE TABLE vac002 (
    syskey BIGINT NOT NULL DEFAULT 0,
    autokey BIGINT IDENTITY(1,1) NOT NULL,
    createddate VARCHAR(50) NOT NULL DEFAULT '',
    createduser VARCHAR(50) DEFAULT '',
    t1 VARCHAR(255) NOT NULL DEFAULT '',
    n1 INT DEFAULT 0,
    t2 VARCHAR(255) NOT NULL DEFAULT '',
    t3 NVARCHAR(MAX) NOT NULL DEFAULT '',
    n2 BIGINT DEFAULT 0,
    PRIMARY KEY (syskey),
);
