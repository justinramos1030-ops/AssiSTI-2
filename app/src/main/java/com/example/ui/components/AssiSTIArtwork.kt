package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CampusBlue
import com.example.ui.theme.CardboardShade
import com.example.ui.theme.CardboardTan
import com.example.ui.theme.DeepCircleBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SkyCircleBlue
import com.example.ui.theme.SoftIceBg
import com.example.ui.theme.StiBrightYellow
import com.example.ui.theme.StiGold

@Composable
fun FloatingCirclesBackdrop(
  showBottomRightCircle: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.background(CampusBlue)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      fun drawShadowedCircle(color: Color, radius: Float, center: Offset) {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.24f), Color.Transparent),
            center = Offset(center.x + 8f, center.y + 14f),
            radius = radius * 1.12f,
          ),
          radius = radius * 1.12f,
          center = Offset(center.x + 8f, center.y + 14f),
        )
        drawCircle(color = color, radius = radius, center = center)
      }

      // Top-left large deep royal blue circle
      drawShadowedCircle(
        color = DeepCircleBlue,
        radius = size.width * 0.42f,
        center = Offset(size.width * 0.10f, size.height * 0.04f),
      )
      // Top-right sky-blue circle
      drawShadowedCircle(
        color = SkyCircleBlue,
        radius = size.width * 0.23f,
        center = Offset(size.width * 0.77f, size.height * 0.03f),
      )
      // Mid-right deep royal blue circle
      drawShadowedCircle(
        color = DeepCircleBlue,
        radius = size.width * 0.44f,
        center = Offset(size.width * 1.05f, size.height * 0.30f),
      )
      // Mid-left sky-blue circle
      drawShadowedCircle(
        color = SkyCircleBlue,
        radius = size.width * 0.36f,
        center = Offset(-size.width * 0.14f, size.height * 0.45f),
      )
      if (showBottomRightCircle) {
        // Bottom-right sky-blue circle (Canva Pages 2 & 17)
        drawShadowedCircle(
          color = SkyCircleBlue,
          radius = size.width * 0.35f,
          center = Offset(size.width * 0.90f, size.height * 0.90f),
        )
      }
    }
  }
}

@Composable
fun CampusWatermarkBackground(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier.background(
      Brush.verticalGradient(
        colors = listOf(Color(0xFFDCE2F1), SoftIceBg, Color(0xFFD2DAF0)),
      )
    )
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val markColor = Color(0xFFB7C3DB)
      val markLight = Color(0xFFC5CFE4)
      val stroke = Stroke(width = 7.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
      val thinStroke = Stroke(width = 5.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

      fun drawDetailedWrench(center: Offset, angleDeg: Float, scale: Float) {
        rotate(degrees = angleDeg, pivot = center) {
          val shaftW = 28f * scale
          val shaftH = 190f * scale
          drawRoundRect(
            color = markColor,
            topLeft = Offset(center.x - shaftW / 2f, center.y - shaftH / 2f),
            size = Size(shaftW, shaftH),
            cornerRadius = CornerRadius(12f * scale, 12f * scale),
            style = stroke,
          )
          // Inner grip slot
          drawRoundRect(
            color = markLight,
            topLeft = Offset(center.x - 5f * scale, center.y - shaftH * 0.32f),
            size = Size(10f * scale, shaftH * 0.64f),
            cornerRadius = CornerRadius(5f * scale, 5f * scale),
          )
          // Top C-jaw
          drawArc(
            color = markColor,
            startAngle = -55f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(center.x - 42f * scale, center.y - shaftH / 2f - 56f * scale),
            size = Size(84f * scale, 84f * scale),
            style = stroke,
          )
          // Bottom ring jaw
          drawCircle(
            color = markColor,
            radius = 36f * scale,
            center = Offset(center.x, center.y + shaftH / 2f + 24f * scale),
            style = stroke,
          )
        }
      }

      // Top-left & Top-right detailed wrench watermarks (matching Canva Page 3)
      drawDetailedWrench(Offset(size.width * 0.04f, size.height * 0.22f), -32f, 1.0f)
      drawDetailedWrench(Offset(size.width * 0.90f, size.height * 0.20f), 40f, 1.05f)
      drawDetailedWrench(Offset(size.width * 0.96f, size.height * 0.28f), 115f, 0.85f)

      // Bottom-left detailed ringing bell watermark (matching Canva Pages 3, 4, 7, 18)
      val bellCenter = Offset(size.width * 0.09f, size.height * 0.66f)
      rotate(degrees = 14f, pivot = bellCenter) {
        // Bell dome
        val dome = Path().apply {
          moveTo(bellCenter.x - 95f, bellCenter.y + 42f)
          cubicTo(
            bellCenter.x - 90f, bellCenter.y - 68f,
            bellCenter.x - 42f, bellCenter.y - 98f,
            bellCenter.x, bellCenter.y - 98f,
          )
          cubicTo(
            bellCenter.x + 42f, bellCenter.y - 98f,
            bellCenter.x + 90f, bellCenter.y - 68f,
            bellCenter.x + 95f, bellCenter.y + 42f,
          )
        }
        drawPath(dome, color = markColor, style = stroke)
        // Top handle loop
        drawCircle(
          color = markColor,
          radius = 16f,
          center = Offset(bellCenter.x, bellCenter.y - 114f),
          style = stroke,
        )
        // Flared bottom lip
        drawRoundRect(
          color = markColor,
          topLeft = Offset(bellCenter.x - 114f, bellCenter.y + 42f),
          size = Size(228f, 26f),
          cornerRadius = CornerRadius(13f, 13f),
          style = stroke,
        )
        // Clapper ball
        drawArc(
          color = markColor,
          startAngle = 0f,
          sweepAngle = 180f,
          useCenter = false,
          topLeft = Offset(bellCenter.x - 24f, bellCenter.y + 52f),
          size = Size(48f, 44f),
          style = stroke,
        )
        // Sound wave arcs on right
        drawArc(
          color = markColor,
          startAngle = -55f,
          sweepAngle = 75f,
          useCenter = false,
          topLeft = Offset(bellCenter.x - 70f, bellCenter.y - 120f),
          size = Size(200f, 200f),
          style = thinStroke,
        )
        drawArc(
          color = markColor,
          startAngle = -55f,
          sweepAngle = 75f,
          useCenter = false,
          topLeft = Offset(bellCenter.x - 55f, bellCenter.y - 140f),
          size = Size(240f, 240f),
          style = thinStroke,
        )
      }

      // Bottom-right detailed magnifying glass watermark (matching Canva Pages 3, 4, 7, 18)
      val glassCenter = Offset(size.width * 0.84f, size.height * 0.67f)
      drawCircle(
        color = markColor,
        radius = 104f,
        center = glassCenter,
        style = stroke,
      )
      drawCircle(
        color = markColor,
        radius = 84f,
        center = glassCenter,
        style = thinStroke,
      )
      // Inner glass reflection arc
      drawArc(
        color = markColor,
        startAngle = 200f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(glassCenter.x - 62f, glassCenter.y - 62f),
        size = Size(124f, 124f),
        style = thinStroke,
      )
      // Handle extending down-right
      rotate(degrees = 45f, pivot = glassCenter) {
        drawRoundRect(
          color = markColor,
          topLeft = Offset(glassCenter.x + 104f, glassCenter.y - 10f),
          size = Size(34f, 20f),
          cornerRadius = CornerRadius(6f, 6f),
          style = thinStroke,
        )
        drawRoundRect(
          color = markColor,
          topLeft = Offset(glassCenter.x + 138f, glassCenter.y - 16f),
          size = Size(135f, 32f),
          cornerRadius = CornerRadius(14f, 14f),
          style = stroke,
        )
      }
    }
    content()
  }
}

@Composable
fun StiGlobeBadge(
  badgeSize: Dp = 96.dp,
  circleColor: Color = StiBrightYellow,
  showCircle: Boolean = true,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(badgeSize)
      .shadow(if (showCircle) 10.dp else 4.dp, CircleShape)
      .clip(CircleShape)
      .background(if (showCircle) circleColor else StiBrightYellow),
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.sti_logo),
      contentDescription = "STI College Logo",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
fun AssiSTIBoxBadge(
  badgeSize: Dp = 156.dp,
  showStaMesaLabel: Boolean = true,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(badgeSize)
      .shadow(12.dp, CircleShape)
      .clip(CircleShape)
      .background(DeepCircleBlue),
    contentAlignment = Alignment.Center,
  ) {
    AssiSTISupplyBoxArt(
      showStaMesa = showStaMesaLabel,
      raysColor = PureWhite,
      modifier = Modifier
        .size(badgeSize * 0.88f)
        .padding(top = 4.dp),
    )
  }
}

private fun DrawScope.drawHalftoneRay(
  p1: Offset,
  p2: Offset,
  p3: Offset,
  p4: Offset,
  color: Color,
) {
  val path = Path().apply {
    moveTo(p1.x, p1.y)
    lineTo(p2.x, p2.y)
    lineTo(p3.x, p3.y)
    lineTo(p4.x, p4.y)
    close()
  }
  drawPath(path, color = color.copy(alpha = 0.22f))
  drawPath(path, color = color.copy(alpha = 0.85f), style = Stroke(width = 2.5f))
  clipPath(path) {
    val minX = minOf(p1.x, p2.x, p3.x, p4.x)
    val maxX = maxOf(p1.x, p2.x, p3.x, p4.x)
    val minY = minOf(p1.y, p2.y, p3.y, p4.y)
    val maxY = maxOf(p1.y, p2.y, p3.y, p4.y)
    val step = 10f
    var y = minY
    while (y <= maxY) {
      var x = minX
      while (x <= maxX) {
        drawCircle(color = color.copy(alpha = 0.65f), radius = 2.4f, center = Offset(x, y))
        x += step
      }
      y += step
    }
  }
}

@Composable
fun AssiSTISupplyBoxArt(
  showStaMesa: Boolean = false,
  raysColor: Color = DeepCircleBlue,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // Halftone dotted trapezoid rays (Canva Page 1 blue rays / Page 2 & 17 white rays)
      drawHalftoneRay(
        p1 = Offset(w * 0.50f, h * 0.24f),
        p2 = Offset(w * 0.49f, h * 0.02f),
        p3 = Offset(w * 0.63f, h * 0.02f),
        p4 = Offset(w * 0.61f, h * 0.24f),
        color = raysColor,
      )
      drawHalftoneRay(
        p1 = Offset(w * 0.65f, h * 0.30f),
        p2 = Offset(w * 0.78f, h * 0.08f),
        p3 = Offset(w * 0.90f, h * 0.16f),
        p4 = Offset(w * 0.74f, h * 0.36f),
        color = raysColor,
      )
      drawHalftoneRay(
        p1 = Offset(w * 0.76f, h * 0.42f),
        p2 = Offset(w * 0.96f, h * 0.33f),
        p3 = Offset(w * 0.99f, h * 0.47f),
        p4 = Offset(w * 0.79f, h * 0.51f),
        color = raysColor,
      )

      // Broom bristles & blue handle cap peeking out
      val broomPath = Path().apply {
        moveTo(w * 0.28f, h * 0.50f)
        lineTo(w * 0.23f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.16f)
        lineTo(w * 0.58f, h * 0.50f)
        close()
      }
      drawPath(broomPath, color = Color(0xFF5A3B24))
      for (i in 1..8) {
        val t = i / 9f
        val topX = w * (0.23f + t * 0.33f)
        val topY = h * (0.20f - t * 0.04f)
        val botX = w * (0.28f + t * 0.30f)
        drawLine(
          color = Color(0xFF3A2414),
          start = Offset(topX, topY),
          end = Offset(botX, h * 0.50f),
          strokeWidth = 2f,
        )
      }
      drawRoundRect(
        color = Color(0xFF28559A),
        topLeft = Offset(w * 0.27f, h * 0.43f),
        size = Size(w * 0.31f, h * 0.08f),
        cornerRadius = CornerRadius(6f, 6f),
      )

      // Detailed metallic wrench on the left
      rotate(degrees = -28f, pivot = Offset(w * 0.22f, h * 0.38f)) {
        drawRoundRect(
          color = Color(0xFFD5DCE4),
          topLeft = Offset(w * 0.185f, h * 0.25f),
          size = Size(w * 0.07f, h * 0.34f),
          cornerRadius = CornerRadius(8f, 8f),
        )
        drawRoundRect(
          color = Color(0xFF8C96A4),
          topLeft = Offset(w * 0.205f, h * 0.29f),
          size = Size(w * 0.03f, h * 0.24f),
          cornerRadius = CornerRadius(4f, 4f),
        )
        drawCircle(
          color = Color(0xFFDDE3EA),
          radius = w * 0.072f,
          center = Offset(w * 0.22f, h * 0.23f),
        )
        drawCircle(
          color = DeepCircleBlue,
          radius = w * 0.032f,
          center = Offset(w * 0.22f, h * 0.20f),
        )
      }

      // Pen & pencil peeking next to glasses
      rotate(degrees = -12f, pivot = Offset(w * 0.33f, h * 0.43f)) {
        drawRoundRect(
          color = Color(0xFF202634),
          topLeft = Offset(w * 0.31f, h * 0.34f),
          size = Size(w * 0.03f, h * 0.18f),
          cornerRadius = CornerRadius(3f, 3f),
        )
      }
      rotate(degrees = 6f, pivot = Offset(w * 0.42f, h * 0.43f)) {
        drawRoundRect(
          color = StiGold,
          topLeft = Offset(w * 0.41f, h * 0.33f),
          size = Size(w * 0.025f, h * 0.19f),
          cornerRadius = CornerRadius(3f, 3f),
        )
      }

      // Black-rimmed eyeglasses peeking out
      drawCircle(
        color = Color(0xFF1E2430),
        radius = w * 0.058f,
        center = Offset(w * 0.39f, h * 0.42f),
        style = Stroke(width = 5f),
      )
      drawCircle(
        color = PureWhite.copy(alpha = 0.28f),
        radius = w * 0.052f,
        center = Offset(w * 0.39f, h * 0.42f),
      )

      // White smartphone peeking out center-right
      rotate(degrees = 10f, pivot = Offset(w * 0.56f, h * 0.42f)) {
        drawRoundRect(
          color = PureWhite,
          topLeft = Offset(w * 0.46f, h * 0.30f),
          size = Size(w * 0.20f, h * 0.28f),
          cornerRadius = CornerRadius(14f, 14f),
        )
        drawRoundRect(
          color = Color(0xFFD0D5DD),
          topLeft = Offset(w * 0.46f, h * 0.30f),
          size = Size(w * 0.20f, h * 0.28f),
          cornerRadius = CornerRadius(14f, 14f),
          style = Stroke(width = 2f),
        )
        drawRoundRect(
          color = Color(0xFF161B26),
          topLeft = Offset(w * 0.485f, h * 0.35f),
          size = Size(w * 0.15f, h * 0.18f),
          cornerRadius = CornerRadius(4f, 4f),
        )
        drawCircle(
          color = Color(0xFF475467),
          radius = w * 0.014f,
          center = Offset(w * 0.56f, h * 0.325f),
        )
      }

      // Purple water bottle with cap peeking out right
      rotate(degrees = 8f, pivot = Offset(w * 0.72f, h * 0.42f)) {
        drawRoundRect(
          color = Color(0xFF6B3FA0),
          topLeft = Offset(w * 0.66f, h * 0.34f),
          size = Size(w * 0.15f, h * 0.24f),
          cornerRadius = CornerRadius(12f, 12f),
        )
        drawRoundRect(
          color = Color(0xFF532C82),
          topLeft = Offset(w * 0.68f, h * 0.30f),
          size = Size(w * 0.11f, h * 0.05f),
          cornerRadius = CornerRadius(6f, 6f),
        )
        drawRoundRect(
          color = PureWhite.copy(alpha = 0.22f),
          topLeft = Offset(w * 0.68f, h * 0.37f),
          size = Size(w * 0.025f, h * 0.14f),
          cornerRadius = CornerRadius(4f, 4f),
        )
      }

      // 3D Cardboard box body + inner dark rim + flaps
      val boxTop = h * 0.50f
      drawRect(
        color = Color(0xFF8A5936),
        topLeft = Offset(w * 0.15f, boxTop - h * 0.02f),
        size = Size(w * 0.70f, h * 0.06f),
      )
      drawRoundRect(
        color = CardboardTan,
        topLeft = Offset(w * 0.15f, boxTop),
        size = Size(w * 0.70f, h * 0.47f),
        cornerRadius = CornerRadius(8f, 8f),
      )
      // Subtle top shadow under front flap
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF9F6A43).copy(alpha = 0.55f), Color.Transparent),
          startY = boxTop,
          endY = boxTop + h * 0.10f,
        ),
        topLeft = Offset(w * 0.15f, boxTop),
        size = Size(w * 0.70f, h * 0.10f),
      )
      // Left & Right side flaps
      val leftFlap = Path().apply {
        moveTo(w * 0.15f, boxTop)
        lineTo(w * 0.05f, boxTop - h * 0.06f)
        lineTo(w * 0.34f, boxTop - h * 0.06f)
        lineTo(w * 0.46f, boxTop)
        close()
      }
      drawPath(leftFlap, color = CardboardShade)
      val rightFlap = Path().apply {
        moveTo(w * 0.85f, boxTop)
        lineTo(w * 0.95f, boxTop - h * 0.06f)
        lineTo(w * 0.66f, boxTop - h * 0.06f)
        lineTo(w * 0.54f, boxTop)
        close()
      }
      drawPath(rightFlap, color = CardboardShade)
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(bottom = if (showStaMesa) 10.dp else 14.dp),
    ) {
      val logoShadow = androidx.compose.ui.graphics.Shadow(
        color = Color.Black.copy(alpha = 0.35f),
        offset = Offset(2f, 4f),
        blurRadius = 6f,
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Assi",
          style = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (showStaMesa) 21.sp else 36.sp,
            color = PureWhite,
            shadow = logoShadow,
          ),
        )
        Text(
          text = "STI",
          style = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (showStaMesa) 21.sp else 36.sp,
            color = StiBrightYellow,
            shadow = logoShadow,
          ),
        )
      }
      if (showStaMesa) {
        Text(
          text = "STA. MESA",
          style = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = PureWhite,
            letterSpacing = 0.8.sp,
            shadow = logoShadow,
          ),
        )
      }
    }
  }
}
