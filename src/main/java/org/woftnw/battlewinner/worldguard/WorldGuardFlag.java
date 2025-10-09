package org.woftnw.battlewinner.worldguard;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.jetbrains.annotations.NotNull;

public interface WorldGuardFlag {

	@NotNull
	StateFlag getFlag();

	void setFlag(StateFlag flag);

	Object getDefault();

	String getName();

}
