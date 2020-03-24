ALTER TABLE `fgame`.`fg_csgo_live_battle`
CHANGE COLUMN `live` `is_live` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `frozen` `is_frozen` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `bomb_planted` `is_bomb_planted` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_dota_battle_team_stats`
CHANGE COLUMN `win` `is_win` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `first_blood` `is_first_blood` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `ten_kills` `is_ten_kills` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_dota_item`
CHANGE COLUMN `recipe` `is_recipe` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `secret_shop` `is_secret_shop` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `side_shop` `is_side_shop` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_lol_battle_team_stats`
CHANGE COLUMN `win` `is_win` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `first_big_dragon` `is_first_big_dragon` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `first_small_dragon` `is_first_small_dragon` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `first_blood` `is_first_blood` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `five_kills` `is_five_kills` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `ten_kills` `is_ten_kills` TINYINT(4) NULL DEFAULT NULL ,
CHANGE COLUMN `first_tower` `is_first_tower` TINYINT(4) NULL DEFAULT NULL ;


ALTER TABLE `fgame`.`fg_lol_bet`
CHANGE COLUMN `rolling` `is_rolling` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_dota_bet`
CHANGE COLUMN `rolling` `is_rolling` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_csgo_bet`
CHANGE COLUMN `rolling` `is_rolling` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_kog_bet`
CHANGE COLUMN `rolling` `is_rolling` TINYINT(4) NULL DEFAULT NULL ;

ALTER TABLE `fgame`.`fg_csgo_battle`
ADD UNIQUE INDEX `battle_id_UNIQUE` (`battle_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_csgo_bet`
ADD UNIQUE INDEX `bet_id_UNIQUE` (`bet_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_csgo_live_battle`
ADD UNIQUE INDEX `battle_id_UNIQUE` (`battle_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_csgo_top_player`
CHANGE COLUMN `player_id` `player_id` VARCHAR(45) NOT NULL ,
ADD UNIQUE INDEX `player_id_UNIQUE` (`player_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_csgo_top_team`
CHANGE COLUMN `team_id` `team_id` VARCHAR(45) NOT NULL ,
ADD UNIQUE INDEX `team_id_UNIQUE` (`team_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_csgo_top_weapon`
CHANGE COLUMN `weapon_name` `weapon_name` VARCHAR(45) NOT NULL ,
ADD UNIQUE INDEX `weapon_name_UNIQUE` (`weapon_name` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_dota_bet`
ADD UNIQUE INDEX `bet_id_UNIQUE` (`bet_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_kog_item`
CHANGE COLUMN `item_id` `item_id` VARCHAR(45) NOT NULL ,
ADD UNIQUE INDEX `item_id_UNIQUE` (`item_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_lol_battle`
ADD UNIQUE INDEX `battle_id_UNIQUE` (`battle_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_lol_battle_player_stats`
ADD UNIQUE INDEX `stats_id_UNIQUE` (`stats_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_lol_battle_team_stats`
ADD UNIQUE INDEX `stats_id_UNIQUE` (`stats_id` ASC) VISIBLE;

ALTER TABLE `fgame`.`fg_dota_bet`
ADD UNIQUE INDEX `bet_id_UNIQUE` (`bet_id` ASC) VISIBLE;