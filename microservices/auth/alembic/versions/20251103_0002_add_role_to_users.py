from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision = '20251103_0002'
down_revision = '20251103_0001'
branch_labels = None
depends_on = None


def upgrade() -> None:
    # Add 'role' column to auth.users with default 'user'
    op.add_column(
        'users',
        sa.Column('role', sa.String(), nullable=True, server_default='user'),
        schema='auth'
    )
    # Backfill existing rows and then enforce NOT NULL
    op.execute("UPDATE auth.users SET role = 'user' WHERE role IS NULL")
    op.alter_column('users', 'role', existing_type=sa.String(), nullable=False, schema='auth', server_default=None)


def downgrade() -> None:
    op.drop_column('users', 'role', schema='auth')
