#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14

// varying input variables from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;


uniform float iTime;
uniform vec2 iResolution;
uniform vec2 iCenter;

uniform vec3 iAlertColor;
uniform vec2 iAlertCenter;
uniform float iAlertInnerSize;
uniform float iAlertOuterSize;
uniform bool iAlert;



// a special uniform for textures 
uniform sampler2D u_texture;

void main()
{
  //Sawtooth function to pulse from centre.
  float offset = (iTime- floor(iTime))/iTime;
  float CurrentTime = (iTime)*(offset);

  vec3 WaveParams = vec3(10.0, 0.8, 0.1 );


  //Use this if you want to place the centre with the mouse instead
  //vec2 WaveCentre = vec2( iMouse.xy / iResolution.xy );

  vec2 WaveCentre = vec2(iCenter.x/iResolution.x, iCenter.y/iResolution.y);

  vec2 texCoord = v_texCoords.st;

  float Dist = distance(texCoord, WaveCentre);




  vec4 Color = texture2D(u_texture, v_texCoords.st);

  //Only distort the pixels within the parameter distance from the centre
  if ((Dist <= ((CurrentTime) + (WaveParams.z))) &&
  (Dist >= ((CurrentTime) - (WaveParams.z))))
  {
    //The pixel offset distance based on the input parameters
    float Diff = (Dist - CurrentTime);
    float ScaleDiff = (1.0 - pow(abs(Diff * WaveParams.x), WaveParams.y));
    float DiffTime = (Diff  * ScaleDiff);

    //The direction of the distortion
    vec2 DiffTexCoord = normalize(texCoord - WaveCentre);

    //Perform the distortion and reduce the effect over time
    texCoord += ((DiffTexCoord * DiffTime) / (CurrentTime * Dist * 40.0));
    Color = texture2D(u_texture, texCoord);

    //Blow out the color and reduce the effect over time
    //Color += (Color * ScaleDiff) / (CurrentTime * Dist * 40.0);
  }

    if(iAlertInnerSize != -1.0){
      float rEnable = 1.0;
      float gEnable = 0.0;
      float bEnable = 0.0;

      float OuterVig = 0.0;// Position for the Outer vignette

      float InnerVig = 0.75;// Position for the inner Vignette Ring

      vec2 uv = texCoord;

      vec4 color = Color;

      vec2 center = vec2(iAlertCenter.x/iResolution.x, iAlertCenter.y/iResolution.y);// Center of Screen

      float dist  = distance(center, uv)*1.414213;// Distance  between center and the current Uv. Multiplyed by 1.414213 to fit in the range of 0.0 to 1.0

      float vig = clamp((0.0-dist) / (0.0-iAlertInnerSize), 0.0, 1.0);// Generate the Vignette with Clamp which go from outer Viggnet ring to inner vignette ring with smooth steps



      if (vig == 1.0){
        gl_FragColor = vec4(Color.rgb, texture2D(u_texture, texCoord).a);
      }
      else {
        color *= vig;
        color.r += (1.0-vig) * iAlertColor.r;
        color.g += (1.0-vig) * iAlertColor.g;
        color.b += (1.0-vig) * iAlertColor.b;
        gl_FragColor = vec4(color.rgb, texture2D(u_texture, texCoord).a);

      }
    }else {
      gl_FragColor = vec4(Color.rgb, texture2D(u_texture, texCoord).a);
    }

}
