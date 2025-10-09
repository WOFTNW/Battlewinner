package org.woftnw.battlewinner.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import org.woftnw.battlewinner.Battlewinner;
import org.woftnw.battlewinner.worldguard.flag.RespiratorFlag;
import org.woftnw.battlewinner.worldguard.handler.RespiratorFlagHandler;

public class WorldGuardAccessor {

	public final WorldGuardFlag[] flags = {
			new RespiratorFlag()
	};

	public void load() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		for (WorldGuardFlag flag : flags) {
			if (flag instanceof StateFlag) {
				try {
					// create a flag
					StateFlag newFlag = new StateFlag(flag.getName(), (Boolean) flag.getDefault());
					registry.register(newFlag);
					flag.setFlag(newFlag); // only set our flag if there was no error
				} catch (FlagConflictException e) {
					// some other plugin registered a flag by the same name already.
					// you can use the existing flag, but this may cause conflicts - be sure to check type
					Flag<?> existing = registry.get(flag.getName());
					if (existing instanceof StateFlag existingStateFlag) {
						flag.setFlag(existingStateFlag);
					} else {
						// types don't match - this is bad news! some other plugin conflicts with you
						// hopefully this never actually happens
						Battlewinner.getInstance().getLogger().warning("Flag " + flag.getFlag().getName() + " is registered by another plugin!");
					}
				}
			} else {
				Battlewinner.getInstance().getLogger().warning("Flag " + flag.getFlag().getName() + " is not a StateFlag!");
			}
		}
		SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
		// second param allows for ordering of handlers - see the JavaDocs
		sessionManager.registerHandler(RespiratorFlagHandler.FACTORY, null);
	}

	public WorldGuardFlag getFlag(String name) {
		for (WorldGuardFlag flag : flags) {
			if (name.equals(flag.getName())) return flag;
		}
		return null;
	}

}
