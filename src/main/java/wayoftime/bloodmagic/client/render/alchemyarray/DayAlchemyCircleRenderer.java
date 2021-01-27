package wayoftime.bloodmagic.client.render.alchemyarray;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import wayoftime.bloodmagic.client.render.BloodMagicRenderer;
import wayoftime.bloodmagic.client.render.BloodMagicRenderer.Model2D;
import wayoftime.bloodmagic.client.render.RenderResizableQuadrilateral;
import wayoftime.bloodmagic.tile.TileAlchemyArray;

public class DayAlchemyCircleRenderer extends AlchemyArrayRenderer
{
	private final ResourceLocation spikesResource;
	private final ResourceLocation circleResource;

	public DayAlchemyCircleRenderer(ResourceLocation arrayResource, ResourceLocation spikesResource, ResourceLocation circleResource)
	{
		super(arrayResource);
		this.spikesResource = spikesResource;
		this.circleResource = circleResource;
	}

	@Override
	public float getRotation(float craftTime)
	{
		return 0;
	}

	public float getSecondaryRotation(float craftTime)
	{
		float offset = 2;
		if (craftTime >= offset)
		{
			float modifier = (craftTime - offset) * (craftTime - offset) * 0.05f;
			return modifier * 1f;
		}
		return 0;
	}

	public float getVerticalOffset(float craftTime)
	{
		if (craftTime >= 40)
		{
			if (craftTime <= 100)
			{
				return (float) (-0.4 + (0.4) * Math.pow((craftTime - 40) / 60f, 3));
			} else
			{
				return 0;
			}
		}
		return -0.4f;
	}

	public float getSizeModifier(float craftTime)
	{
		return 1.0f;
	}

	public float getSecondarySizeModifier(float craftTime)
	{
		if (craftTime >= 40)
		{
			if (craftTime <= 160)
			{
				return (float) ((2f) * Math.pow((craftTime - 40) / 120f, 3));
			} else
			{
				return 2;
			}
		}

		return 0;
	}

	public float getTertiarySizeModifier(float craftTime)
	{
		if (craftTime >= 40)
		{
			if (craftTime <= 100)
			{
				return (float) ((1f) * Math.pow((craftTime - 40) / 60f, 3));
			} else
			{
				return 1;
			}
		}

		return 0;
	}

	public float getSpikeVerticalOffset(float craftTime)
	{
		if (craftTime >= 40)
		{
			if (craftTime <= 100)
			{
				return (float) (-0.4 + (0.4) * Math.pow((craftTime - 40) / 60f, 3));
			} else if (craftTime <= 140)
			{
				return -0.01f * (craftTime - 100);
			} else
			{
				return -0.4f;
			}
		}
		return -0.4f;
	}

	public float getCentralCircleOffset(float craftTime)
	{
		if (craftTime >= 40)
		{
			if (craftTime <= 100)
			{
				return (float) (-0.4 + (0.4) * Math.pow((craftTime - 40) / 60f, 3));
			} else if (craftTime <= 140)
			{
				return 0.01f * (craftTime - 100);
			} else
			{
				return 0.4f;
			}
		}
		return -0.4f;
	}

	public void renderAt(TileAlchemyArray tileArray, double x, double y, double z, float craftTime, MatrixStack matrixStack, IRenderTypeBuffer renderer, int combinedLightIn, int combinedOverlayIn)
	{
		matrixStack.push();

		matrixStack.translate(0.5, 0.5, 0.5);

		float rot = getRotation(craftTime);
		float secondaryRot = getSecondaryRotation(craftTime);

		float size = 1.0F * getSizeModifier(craftTime);
		Direction rotation = tileArray.getRotation();

		matrixStack.push();
		matrixStack.translate(0, getVerticalOffset(craftTime), 0);
		matrixStack.rotate(new Quaternion(Direction.UP.toVector3f(), -rotation.getHorizontalAngle(), true));

		matrixStack.push();

		matrixStack.rotate(new Quaternion(Direction.NORTH.toVector3f(), rot, true));
		matrixStack.rotate(new Quaternion(Direction.UP.toVector3f(), secondaryRot, true));
//		matrixStack.rotate(new Quaternion(Direction.EAST.toVector3f(), secondaryRot * 0.45812f, true));

		IVertexBuilder twoDBuffer = renderer.getBuffer(RenderType.getEntityTranslucent(arrayResource));
		Model2D arrayModel = new BloodMagicRenderer.Model2D();
		arrayModel.minX = -0.5;
		arrayModel.maxX = +0.5;
		arrayModel.minY = -0.5;
		arrayModel.maxY = +0.5;
		arrayModel.resource = arrayResource;

		matrixStack.scale(size, size, size);

		RenderResizableQuadrilateral.INSTANCE.renderSquare(arrayModel, matrixStack, twoDBuffer, 0xFFFFFFFF, 0x00F000F0, combinedOverlayIn);

		matrixStack.pop();
		matrixStack.pop();

		matrixStack.push();
		matrixStack.translate(0, getSpikeVerticalOffset(craftTime), 0);
		matrixStack.rotate(new Quaternion(Direction.UP.toVector3f(), -rotation.getHorizontalAngle(), true));

		matrixStack.push();

		matrixStack.rotate(new Quaternion(Direction.NORTH.toVector3f(), rot, true));
		matrixStack.rotate(new Quaternion(Direction.UP.toVector3f(), -secondaryRot, true));
//		matrixStack.rotate(new Quaternion(Direction.EAST.toVector3f(), secondaryRot * 0.45812f, true));

		twoDBuffer = renderer.getBuffer(RenderType.getEntityTranslucent(spikesResource));
		arrayModel = new BloodMagicRenderer.Model2D();
		arrayModel.minX = -0.5;
		arrayModel.maxX = +0.5;
		arrayModel.minY = -0.5;
		arrayModel.maxY = +0.5;
		arrayModel.resource = spikesResource;

		float secondarySize = 1.0F * getSecondarySizeModifier(craftTime);

		matrixStack.scale(secondarySize, secondarySize, secondarySize);

		int colorWanted = 0xFFFFFFFF;

		RenderResizableQuadrilateral.INSTANCE.renderSquare(arrayModel, matrixStack, twoDBuffer, colorWanted, 0x00F000F0, combinedOverlayIn);

		matrixStack.pop();
		matrixStack.pop();

		matrixStack.push();
		matrixStack.translate(0, getCentralCircleOffset(craftTime), 0);
		matrixStack.rotate(new Quaternion(Direction.UP.toVector3f(), -rotation.getHorizontalAngle(), true));

		matrixStack.push();

		matrixStack.rotate(new Quaternion(Direction.NORTH.toVector3f(), rot, true));
		matrixStack.rotate(new Quaternion(Direction.UP.toVector3f(), -secondaryRot, true));
//		matrixStack.rotate(new Quaternion(Direction.EAST.toVector3f(), secondaryRot * 0.45812f, true));

		twoDBuffer = renderer.getBuffer(RenderType.getEntityTranslucent(circleResource));
		arrayModel = new BloodMagicRenderer.Model2D();
		arrayModel.minX = -0.5;
		arrayModel.maxX = +0.5;
		arrayModel.minY = -0.5;
		arrayModel.maxY = +0.5;
		arrayModel.resource = circleResource;

		float tertiarySize = 1.0F * getTertiarySizeModifier(craftTime);

		matrixStack.scale(tertiarySize, tertiarySize, tertiarySize);

		colorWanted = 0xFFFFFFFF;

		RenderResizableQuadrilateral.INSTANCE.renderSquare(arrayModel, matrixStack, twoDBuffer, colorWanted, 0x00F000F0, combinedOverlayIn);

		matrixStack.pop();
		matrixStack.pop();
		matrixStack.pop();
	}
}
