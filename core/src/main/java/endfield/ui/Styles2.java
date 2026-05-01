package endfield.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import endfield.ui.HoldImageButton.HoldImageButtonStyle;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

public final class Styles2 {
	public static TextFieldStyle scriptArea;
	public static ButtonStyle right;
	public static TextButtonStyle round, toggleCenterText;

	public static ImageButtonStyle modImageStyle;

	public static ImageButtonStyle clearToggle;

	public static ImageButtonStyle tuImageStyle;
	public static ImageButtonStyle toggleImageStyle;
	public static ImageButtonStyle leftImageStyle, toggleLeftImageStyle;
	public static ImageButtonStyle rightImageStyle, toggleRightImageStyle;
	public static ImageButtonStyle centerImageStyle;

	public static HoldImageButtonStyle defaultHoldImageStyle, modHoldImageStyle;

	public static HoldImageButtonStyle tuHoldImageStyle;
	public static HoldImageButtonStyle teamChanger;

	/** Don't let anyone instantiate this class. */
	private Styles2() {}

	public static void load() {
		//style
		modImageStyle = new ImageButtonStyle(Styles.logici) {{
			down = Styles.flatDown;
			over = Styles.flatOver;
			imageDisabledColor = Color.gray;
			imageUpColor = Color.white;
		}};
		defaultHoldImageStyle = new HoldImageButtonStyle(Styles.defaulti);
		Core.scene.addStyle(HoldImageButtonStyle.class, defaultHoldImageStyle);
		modHoldImageStyle = new HoldImageButtonStyle(modImageStyle);
		//style-2
		clearToggle = new ImageButtonStyle() {{
			down = Styles.flatDown;
			checked = Styles.flatDown;
			up = Styles.black;
			over = Styles.flatOver;
		}};
		//style-3
		scriptArea = new TextFieldStyle() {{
			font = Fonts2.inconsoiata;
			fontColor = Color.white;
			selection = Tex.selection;
			cursor = Tex.cursor;
		}};
		right = new ButtonStyle(Styles.defaultb) {{
			up = Tex2.buttonRight;
			down = Tex2.buttonRightDown;
			over = Tex2.buttonRightOver;
		}};
		round = new TextButtonStyle(Styles.defaultt) {{
			checked = up;
		}};
		toggleCenterText = new TextButtonStyle(Styles.defaultt) {{
			up = Tex2.buttonCenter;
			down = Tex2.buttonCenterDown;
			over = Tex2.buttonCenterOver;
			checked = Tex2.buttonCenterOver;
			disabled = Tex2.buttonCenterDisabled;
		}};
		tuImageStyle = new ImageButtonStyle(Styles.logici) {{
			down = Styles.flatDown;
			over = Styles.flatOver;
			imageDisabledColor = Color.gray;
			imageUpColor = Color.white;
		}};
		toggleImageStyle = new ImageButtonStyle(Styles.defaulti) {{
			checked = Tex.buttonOver;
		}};
		leftImageStyle = new ImageButtonStyle(Styles.defaulti) {{
			up = Tex2.buttonLeft;
			down = Tex2.buttonLeftDown;
			over = Tex2.buttonLeftOver;
		}};
		toggleLeftImageStyle = new ImageButtonStyle(leftImageStyle) {{
			checked = Tex2.buttonLeftOver;
		}};
		rightImageStyle = new ImageButtonStyle(Styles.defaulti) {{
			up = Tex2.buttonRight;
			down = Tex2.buttonRightDown;
			over = Tex2.buttonRightOver;
		}};
		toggleRightImageStyle = new ImageButtonStyle(rightImageStyle) {{
			checked = Tex2.buttonRightOver;
		}};
		centerImageStyle = new ImageButtonStyle(Styles.defaulti) {{
			up = Tex2.buttonCenter;
			down = Tex2.buttonCenterDown;
			over = Tex2.buttonCenterOver;
		}};
		tuHoldImageStyle = new HoldImageButtonStyle(tuImageStyle);
		teamChanger = new HoldImageButtonStyle(Styles.clearNoneTogglei) {{
			down = Tex.whiteui;
			checked = Tex.whiteui;
		}};
	}
}
