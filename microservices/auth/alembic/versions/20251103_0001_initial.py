from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision = '20251103_0001'
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    # users table
    op.create_table(
        'users',
        sa.Column('id', sa.Integer(), primary_key=True, nullable=False),
        sa.Column('name', sa.String(), nullable=True),
        sa.Column('email', sa.String(), nullable=True),
        sa.Column('is_verified', sa.Boolean(), server_default=sa.text('false'), nullable=False),
        sa.Column('otp', sa.Integer(), nullable=True),
        sa.Column('hashed_password', sa.String(), nullable=True),
        sa.Column('date_created', sa.DateTime(timezone=False), server_default=sa.text('NOW()'), nullable=False),
        schema='auth'
    )
    op.create_index('ix_users_email', 'users', ['email'], unique=True, schema='auth')
    op.create_index('ix_users_id', 'users', ['id'], unique=False, schema='auth')

    # addresses table
    op.create_table(
        'addresses',
        sa.Column('id', sa.Integer(), primary_key=True, nullable=False),
        sa.Column('street', sa.String(), nullable=True),
        sa.Column('landmark', sa.String(), nullable=True),
        sa.Column('city', sa.String(), nullable=True),
        sa.Column('country', sa.String(), nullable=True),
        sa.Column('pincode', sa.String(), nullable=True),
        sa.Column('user_id', sa.Integer(), nullable=True),
        sa.Column('latitude', sa.Float(), nullable=True),
        sa.Column('longitude', sa.Float(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['auth.users.id'], name='fk_addresses_user_id_users'),
        schema='auth'
    )
    op.create_index('ix_addresses_id', 'addresses', ['id'], unique=False, schema='auth')


def downgrade() -> None:
    op.drop_index('ix_addresses_id', table_name='addresses', schema='auth')
    op.drop_table('addresses', schema='auth')
    op.drop_index('ix_users_id', table_name='users', schema='auth')
    op.drop_index('ix_users_email', table_name='users', schema='auth')
    op.drop_table('users', schema='auth')
