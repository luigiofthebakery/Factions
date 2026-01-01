package com.massivecraft.factions.cmd;

import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdAutoClaim extends FCommand {
   public CmdAutoClaim() {
      this.aliases.add("autoclaim");
      this.optionalArgs.put("faction", "your");
      this.permission = Permission.CLAIM_AUTO.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      List<String> newArgs = Stream.concat(Stream.of("claim"), this.args.stream()).collect(Collectors.toList());
      P.p.cmdBase.cmdAuto.execute(this.sender, newArgs);
   }
}
