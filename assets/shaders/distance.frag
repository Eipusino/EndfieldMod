uniform sampler2D u_texture;
uniform float u_smoothing;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
	if (u_smoothing > 0.0) {
		float smoothing = 0.25 / u_smoothing;
		vec4 color = texture2D(u_texture, v_texCoords);
		float distance = color.a;
		float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
		gl_FragColor = vec4(v_color.rgb*color.rgb, alpha * v_color.a);
	} else {
		gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
	}
}