-- PROFILE
INSERT INTO profile (id, facebook_link, instagram_link) VALUES (1, 'https://fb.com/john', 'https://instagram.com/john');
INSERT INTO profile (id, facebook_link, instagram_link) VALUES (2, 'https://fb.com/jane', 'https://instagram.com/jane');
INSERT INTO profile (id, facebook_link, instagram_link) VALUES (3, 'https://fb.com/bob', 'https://instagram.com/bob');

-- CUSTOMER (linked to profile)
INSERT INTO customer (id, profile_id, address, customer_type, phone) VALUES (1, 1, '123 Main St', 'REGULAR', '111-111-1111');
INSERT INTO customer (id, profile_id, address, customer_type, phone) VALUES (2, 2, '456 Oak St', 'VIP', '222-222-2222');
INSERT INTO customer (id, profile_id, address, customer_type, phone) VALUES (3, 3, '789 Pine St', 'PREMIUM', '333-333-3333');

-- COMPLAINT (linked to customer)
INSERT INTO complaint (id, customer_id, complaint_type, complaint_status, description)
VALUES (1, 1, 'TECHNICAL', 'OPEN', 'App keeps crashing on login');

INSERT INTO complaint (id, customer_id, complaint_type, complaint_status, description)
VALUES (2, 2, 'BILLING', 'IN_PROGRESS', 'Incorrect charges on invoice');

INSERT INTO complaint (id, customer_id, complaint_type, complaint_status, description)
VALUES (3, 3, 'SERVICE', 'CLOSED', 'Long wait times on support call');

-- ORDER (linked to customer)
INSERT INTO "order" (id, customer_id, order_status, order_details)
VALUES (1, 1, 'PENDING', 'Order #001 - Phone Case');

INSERT INTO "order" (id, customer_id, order_status, order_details)
VALUES (2, 2, 'SHIPPED', 'Order #002 - Laptop Stand');

INSERT INTO "order" (id, customer_id, order_status, order_details)
VALUES (3, 3, 'DELIVERED', 'Order #003 - Wireless Keyboard');

-- PROSPECT (linked to profile)
INSERT INTO prospect (id, profile_id, prospect_status, prospection_type, prospect_details)
VALUES (1, 1, 'NEW', 'EMAIL', 'Sent welcome email');

INSERT INTO prospect (id, profile_id, prospect_status, prospection_type, prospect_details)
VALUES (2, 2, 'CONTACTED', 'CALL', 'Called to pitch new offer');

INSERT INTO prospect (id, profile_id, prospect_status, prospection_type, prospect_details)
VALUES (3, 3, 'INTERESTED', 'MEETING', 'Meeting scheduled for product demo');

-- PROSPECTION (linked to prospect)
INSERT INTO prospection (id, prospect_id, prospection_status, prospection_details)
VALUES (1, 1, 'INITIATED', 'Email opened and clicked');

INSERT INTO prospection (id, prospect_id, prospection_status, prospection_details)
VALUES (2, 2, 'IN_PROGRESS', 'Call was positive, follow-up scheduled');

INSERT INTO prospection (id, prospect_id, prospection_status, prospection_details)
VALUES (3, 3, 'COMPLETED', 'Demo completed, awaiting decision');
