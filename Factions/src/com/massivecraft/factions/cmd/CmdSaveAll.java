package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Permission;

public class CmdSaveAll extends FCommand {
   public CmdSaveAll() {
      this.aliases.add("saveall");
      this.aliases.add("save");
      this.permission = Permission.SAVE.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayers.i.saveToDisc();
      Factions.i.saveToDisc();
      Board.save();
      Conf.save();
      this.msg("<i>Factions saved to disk!", new Object[0]);
   }
}
