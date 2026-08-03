use upvpn_entity::account::Entity as Account;
use upvpn_migration::{
    sea_orm::{ActiveModelTrait, DatabaseConnection, EntityTrait, Set},
    DbErr,
};

#[derive(Debug, Clone)]
pub struct AccountStorage {
    db: DatabaseConnection,
}

impl AccountStorage {
    pub fn new(db: DatabaseConnection) -> Self {
        Self { db }
    }

    pub async fn get_email(&self) -> Result<Option<String>, DbErr> {
        let account = Account::find().one(&self.db).await?;

        Ok(account.map(|a| a.email))
    }

    pub async fn save_email(&self, email: String) -> Result<(), DbErr> {
        // single account: remove any previous account first
        self.remove_all().await?;

        let account = upvpn_entity::account::ActiveModel { email: Set(email) };
        let _ = account.insert(&self.db).await?;

        tracing::info!("account email saved");

        Ok(())
    }

    pub async fn remove_all(&self) -> Result<(), DbErr> {
        let deleted = Account::delete_many().exec(&self.db).await?;

        tracing::info!("deleted accounts #{}", deleted.rows_affected);

        Ok(())
    }
}
