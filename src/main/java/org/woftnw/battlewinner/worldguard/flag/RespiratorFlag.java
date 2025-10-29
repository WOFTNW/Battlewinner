package org.woftnw.battlewinner.worldguard.flag;

import com.sk89q.worldguard.protection.flags.StateFlag;
import org.jetbrains.annotations.NotNull;
import org.woftnw.battlewinner.worldguard.WorldGuardFlag;

public class RespiratorFlag implements WorldGuardFlag {

	private StateFlag RESPIRATOR_FLAG;

	@Override
	public @NotNull StateFlag getFlag() {
		return RESPIRATOR_FLAG;
	}

	@Override
	public void setFlag(StateFlag flag) {
		RESPIRATOR_FLAG = flag;
	}

	@Override
	public Object getDefault() {
		return false;
	}

	@Override
	public String getName() {
		return "respirator";
	}


}
