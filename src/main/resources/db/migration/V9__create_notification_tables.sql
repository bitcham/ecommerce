-- =============================================
-- V9: Notification Domain Tables
-- =============================================

-- Notification table
CREATE TABLE notification (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    link_url    VARCHAR(500),
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_notification_type CHECK (type IN (
        'ORDER', 'DELIVERY', 'PROMOTION', 'REVIEW', 'SYSTEM', 'COUPON'
    ))
);

CREATE INDEX idx_notification_member ON notification(member_id);
CREATE INDEX idx_notification_unread ON notification(member_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notification_created ON notification(created_at DESC);
