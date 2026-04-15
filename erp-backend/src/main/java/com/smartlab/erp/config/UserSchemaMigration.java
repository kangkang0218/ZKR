package com.smartlab.erp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class UserSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    ApplicationRunner migrateUserSchema() {
        return args -> {
            jdbcTemplate.execute("UPDATE sys_user SET role = 'RESEARCH' WHERE role = 'USER'");
            jdbcTemplate.execute("UPDATE sys_user SET role = 'RESEARCH' WHERE role = 'REASE'");
            jdbcTemplate.execute("UPDATE sys_user SET role = 'ALGORITHM' WHERE role = 'ALGO'");
            jdbcTemplate.execute("UPDATE sys_user SET role = 'DATA' WHERE username = 'zhangqi'");
            jdbcTemplate.execute("ALTER TABLE sys_user DROP COLUMN IF EXISTS department");
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS hidden_avatar boolean DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE sys_project_member ADD COLUMN IF NOT EXISTS manager_weight integer DEFAULT 0");
            jdbcTemplate.execute("ALTER TABLE project_execution_plan ADD COLUMN IF NOT EXISTS goal_description TEXT");
            jdbcTemplate.execute("ALTER TABLE project_execution_plan ADD COLUMN IF NOT EXISTS project_tier varchar(10)");
            jdbcTemplate.execute("ALTER TABLE execution_file ADD COLUMN IF NOT EXISTS secondary_category varchar(255)");
            jdbcTemplate.execute("ALTER TABLE execution_file ALTER COLUMN secondary_category TYPE varchar(255)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS execution_archive_folder (id bigserial primary key, project_id varchar(64) not null, folder_type varchar(30) not null, folder_path varchar(255) not null, parent_path varchar(255), created_by_user_id varchar(64), created_at timestamptz not null default now())");
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_execution_archive_folder_unique ON execution_archive_folder(project_id, folder_type, folder_path)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS internal_message (id bigserial primary key, recipient_user_id varchar(64) not null, message_type varchar(40) not null, title varchar(160) not null, content text not null, project_id varchar(64), is_read boolean not null default false, created_at timestamptz not null default now())");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS user_badge (id bigserial primary key, user_id varchar(64) not null, badge_name varchar(80) not null, badge_icon varchar(20), badge_color varchar(20), awarded_by varchar(64), created_at timestamptz not null default now())");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS project_subtask (id bigserial primary key, project_id varchar(64) not null, title varchar(160) not null, description text, assignee_user_id varchar(64), assignee_name varchar(120), sort_order integer default 0, is_completed boolean not null default false, completed_at timestamptz, created_by varchar(64), created_at timestamptz not null default now())");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS product_idea_detail (id bigserial primary key, project_id varchar(64) not null unique, target_users text, core_features text, tech_stack_desc text, test_feedback text)");
            jdbcTemplate.execute("ALTER TABLE product_idea_detail ADD COLUMN IF NOT EXISTS use_case TEXT");
            jdbcTemplate.execute("ALTER TABLE product_idea_detail ADD COLUMN IF NOT EXISTS problem_statement TEXT");
            jdbcTemplate.execute("ALTER TABLE product_idea_detail ADD COLUMN IF NOT EXISTS idea_owner_user_id varchar(64)");
            jdbcTemplate.execute("ALTER TABLE product_idea_detail ADD COLUMN IF NOT EXISTS promotion_ic_user_id varchar(64)");
            jdbcTemplate.execute("ALTER TABLE product_idea_detail ADD COLUMN IF NOT EXISTS meeting_participant_user_ids TEXT");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS project_chat_message (id bigserial primary key, project_id varchar(64) not null, sender_user_id varchar(64) not null, sender_name varchar(120) not null, content text not null, stage_tag varchar(40), created_at timestamptz not null default now())");
            jdbcTemplate.execute("ALTER TABLE project_chat_message ADD COLUMN IF NOT EXISTS stage_tag varchar(40)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_project_chat_project ON project_chat_message(project_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_project_chat_stage ON project_chat_message(stage_tag)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_project_chat_created ON project_chat_message(created_at)");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS research_project_profile (id bigserial primary key, project_id varchar(64) not null unique, research_status varchar(40) not null, innovation_point text, execution_mode varchar(40))");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD COLUMN IF NOT EXISTS idea_text TEXT");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD COLUMN IF NOT EXISTS budget_estimate numeric(19,4)");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD COLUMN IF NOT EXISTS idea_owner_user_id varchar(64)");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD COLUMN IF NOT EXISTS host_user_id varchar(64)");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD COLUMN IF NOT EXISTS chief_engineer_user_id varchar(64)");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD COLUMN IF NOT EXISTS workflow_flags TEXT");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS middleware_asset (id bigserial primary key, name varchar(200) not null, description text, source_project_id varchar(64) not null, repo_url varchar(500), rating varchar(10), created_at timestamptz not null default now())");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS source_flow_type varchar(20)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS source_status varchar(40)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS owner_user_id varchar(64)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS pricing_model varchar(40)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS unit_price numeric(19,4)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS internal_cost_price numeric(19,4)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS market_reference_price numeric(19,4)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS currency varchar(16)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS billing_unit varchar(30)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS version_tag varchar(80)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS lifecycle_status varchar(30)");
            jdbcTemplate.execute("ALTER TABLE middleware_asset ADD COLUMN IF NOT EXISTS extra_metadata TEXT");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS middleware_royalty_roster (id bigserial primary key, middleware_id bigint not null, user_id varchar(64) not null, royalty_ratio numeric(6,4) not null)");
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_middleware_user ON middleware_royalty_roster(middleware_id, user_id)");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS project_member_schedule (id bigserial primary key, project_id varchar(64) not null, user_id varchar(64) not null, expected_start_date timestamptz, expected_end_date timestamptz, actual_end_date timestamptz, task_name varchar(200), expected_output TEXT, is_completed boolean not null default false, manager_confirmed boolean not null default false, manager_confirmed_at timestamptz, created_at timestamptz not null default now(), unique(project_id, user_id))");
            jdbcTemplate.execute("ALTER TABLE project_member_schedule ADD COLUMN IF NOT EXISTS task_name varchar(200)");
            jdbcTemplate.execute("ALTER TABLE project_member_schedule ADD COLUMN IF NOT EXISTS expected_output TEXT");
            jdbcTemplate.execute("ALTER TABLE project_member_schedule ADD COLUMN IF NOT EXISTS manager_confirmed boolean not null default false");
            jdbcTemplate.execute("ALTER TABLE project_member_schedule ADD COLUMN IF NOT EXISTS manager_confirmed_at timestamptz");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS project_git_repository (id bigserial primary key, project_id varchar(64) not null, repository_url varchar(500) not null, access_token text, branch varchar(120), provider varchar(40), created_by_user_id varchar(64) not null, last_test_status varchar(30), last_test_message text, last_tested_at timestamptz, is_active boolean not null default true, created_at timestamptz not null default now(), updated_at timestamptz not null default now())");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_project_git_repo_project ON project_git_repository(project_id)");

            jdbcTemplate.execute("ALTER TABLE sys_project DROP CONSTRAINT IF EXISTS sys_project_research_status_check");
            jdbcTemplate.execute("ALTER TABLE sys_project ADD CONSTRAINT sys_project_research_status_check CHECK (research_status IN ('INIT','BLUEPRINT','EXPANSION','DESIGN','EXECUTION','EVALUATION','ARCHIVE','SHELVED','PROBE','DEEPENING','PRE_EXECUTION','CONSTRUCTION','ARCHIVED_TO_MIDDLEWARE'))");
            jdbcTemplate.execute("ALTER TABLE research_project_profile DROP CONSTRAINT IF EXISTS research_project_profile_research_status_check");
            jdbcTemplate.execute("ALTER TABLE research_project_profile ADD CONSTRAINT research_project_profile_research_status_check CHECK (research_status IN ('INIT','BLUEPRINT','EXPANSION','DESIGN','EXECUTION','EVALUATION','ARCHIVE','SHELVED','PROBE','DEEPENING','PRE_EXECUTION','CONSTRUCTION','ARCHIVED_TO_MIDDLEWARE'))");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS finance_wallet_account ADD COLUMN IF NOT EXISTS total_middleware_profit numeric(15,2) NOT NULL DEFAULT 0");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS finance_wallet_account ADD COLUMN IF NOT EXISTS total_promotion_expense numeric(15,2) NOT NULL DEFAULT 0");
            jdbcTemplate.execute("DO $$ BEGIN IF to_regclass('finance_wallet_account') IS NOT NULL THEN UPDATE finance_wallet_account SET total_middleware_profit = COALESCE(total_royalty_earned, 0) WHERE total_middleware_profit = 0 AND COALESCE(total_royalty_earned, 0) > 0; END IF; END $$;");
            jdbcTemplate.execute("DELETE FROM finance_wallet_account w USING sys_user u WHERE w.user_id = u.user_id AND COALESCE(u.account_domain, 'ERP') <> 'ERP' AND NOT EXISTS (SELECT 1 FROM finance_wallet_transaction t WHERE t.wallet_id = w.id)");
            jdbcTemplate.execute("INSERT INTO finance_wallet_account (user_id, balance, total_dividend_earned, total_royalty_earned, total_middleware_profit, total_promotion_expense, total_adjustment_amount, created_at, updated_at) SELECT u.user_id, 0, 0, 0, 0, 0, 0, NOW(), NOW() FROM sys_user u WHERE COALESCE(u.account_domain, 'ERP') = 'ERP' AND NOT EXISTS (SELECT 1 FROM finance_wallet_account w WHERE w.user_id = u.user_id)");
        };
    }
}
