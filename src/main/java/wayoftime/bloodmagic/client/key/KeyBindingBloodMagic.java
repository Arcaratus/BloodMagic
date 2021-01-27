package wayoftime.bloodmagic.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@OnlyIn(Dist.CLIENT)
public class KeyBindingBloodMagic extends KeyBinding
{
	public KeyBindingBloodMagic(KeyBindings key)
	{
		super(key.getDescription(), -1, "key.bloodmagic.category");

		ClientRegistry.registerKeyBinding(this);
	}
}
