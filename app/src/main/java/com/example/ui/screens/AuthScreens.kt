package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AssiSTIBoxBadge
import com.example.ui.components.AssiSTISupplyBoxArt
import com.example.ui.components.FloatingCirclesBackdrop
import com.example.ui.components.StiGlobeBadge
import com.example.ui.theme.DeepCircleBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MintPill
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.StiGold
import kotlinx.coroutines.launch

private enum class AuthStep {
  PORTAL_SELECT, // Canva Pages 1 & 16
  STUDENT_LOGIN, // Canva Page 2
  ADMIN_LOGIN,   // Canva Page 17
}

@Composable
fun AuthScreen(
  onStudentLogin: suspend (email: String, password: String) -> Result<Unit>,
  onAdminLogin: suspend (adminId: String, password: String) -> Result<Unit>,
) {
  var step by remember { mutableStateOf(AuthStep.PORTAL_SELECT) }
  var studentEmail by remember { mutableStateOf("") }
  var studentPassword by remember { mutableStateOf("") }
  var adminId by remember { mutableStateOf("") }
  var adminPassword by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  if (step != AuthStep.PORTAL_SELECT) {
    BackHandler {
      errorMessage = null
      step = AuthStep.PORTAL_SELECT
    }
  }

  fun submitStudent() {
    if (isLoading) return
    focusManager.clearFocus()
    isLoading = true
    errorMessage = null
    scope.launch {
      val result = onStudentLogin(studentEmail, studentPassword)
      isLoading = false
      result.onFailure { err ->
        errorMessage = err.localizedMessage ?: "Unable to sign in. Please check your email and password."
      }
    }
  }

  fun submitAdmin() {
    if (isLoading) return
    focusManager.clearFocus()
    isLoading = true
    errorMessage = null
    scope.launch {
      val result = onAdminLogin(adminId, adminPassword)
      isLoading = false
      result.onFailure { err ->
        errorMessage = err.localizedMessage ?: "Unable to sign in. Please check your Admin ID and password."
      }
    }
  }

  FloatingCirclesBackdrop(
    showBottomRightCircle = step != AuthStep.PORTAL_SELECT,
    modifier = Modifier.fillMaxSize(),
  )

  when (step) {
    AuthStep.PORTAL_SELECT -> {
      // Canva Pages 1 & 16: STI Badge + Microsoft Log in + Admin Log In + AssiSTI Supply Box
      Box(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 520.dp)
            .align(Alignment.Center)
            .padding(horizontal = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Spacer(Modifier.height(54.dp))
          StiGlobeBadge(badgeSize = 112.dp, circleColor = StiGold)
          Spacer(Modifier.height(34.dp))

          // Microsoft Log in (Student Portal) -> Page 2
          Button(
            onClick = {
              errorMessage = null
              step = AuthStep.STUDENT_LOGIN
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
              containerColor = RoyalNavy,
              contentColor = PureWhite,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .minimumInteractiveComponentSize()
              .testTag("microsoft_login_button"),
          ) {
            Text(
              text = "Microsoft Log in",
              fontFamily = PlusJakartaSansFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
            )
          }

          Spacer(Modifier.height(18.dp))

          // Admin Log In -> Page 17
          Button(
            onClick = {
              errorMessage = null
              step = AuthStep.ADMIN_LOGIN
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
              containerColor = RoyalNavy,
              contentColor = PureWhite,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .minimumInteractiveComponentSize()
              .testTag("admin_login_button"),
          ) {
            Text(
              text = "Admin Log In",
              fontFamily = PlusJakartaSansFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
            )
          }

          Spacer(Modifier.weight(1f))

          AssiSTISupplyBoxArt(
            showStaMesa = false,
            raysColor = DeepCircleBlue,
            modifier = Modifier
              .fillMaxWidth()
              .height(270.dp),
          )
        }
      }
    }

    AuthStep.STUDENT_LOGIN -> {
      // Canva Page 2: AssiSTI Sta. Mesa badge + Email + Password + Royal Blue pill Log In button
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .imePadding(),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          IconButton(
            onClick = {
              focusManager.clearFocus()
              errorMessage = null
              isLoading = false
              step = AuthStep.PORTAL_SELECT
            },
            modifier = Modifier
              .align(Alignment.CenterStart)
              .minimumInteractiveComponentSize()
              .testTag("student_login_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to portal selection",
              tint = PureWhite,
            )
          }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 500.dp)
            .align(Alignment.CenterHorizontally)
            .padding(horizontal = 32.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          AssiSTIBoxBadge(badgeSize = 160.dp, showStaMesaLabel = true)
          Spacer(Modifier.height(30.dp))

          PillAuthField(
            value = studentEmail,
            onValueChange = {
              studentEmail = it
              errorMessage = null
            },
            placeholder = "Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            testTag = "student_email_input",
          )
          Spacer(Modifier.height(14.dp))
          PillPasswordField(
            value = studentPassword,
            onValueChange = {
              studentPassword = it
              errorMessage = null
            },
            placeholder = "Password",
            onDone = { submitStudent() },
            testTag = "student_password_input",
          )

          Spacer(Modifier.height(18.dp))

          // Royal blue oval base + Mint Pill "Log In" matching Canva Page 2
          Surface(
            color = DeepCircleBlue,
            shape = RoundedCornerShape(50),
            shadowElevation = 8.dp,
            modifier = Modifier
              .fillMaxWidth(0.86f)
              .height(74.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Button(
                onClick = { submitStudent() },
                enabled = !isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MintPill,
                  contentColor = RoyalNavy,
                ),
                modifier = Modifier
                  .fillMaxWidth(0.76f)
                  .height(48.dp)
                  .minimumInteractiveComponentSize()
                  .testTag("student_login_submit_button"),
              ) {
                if (isLoading) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = RoyalNavy,
                    strokeWidth = 2.dp,
                  )
                } else {
                  Text(
                    text = "Log In",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                  )
                }
              }
            }
          }

          if (errorMessage != null) {
            Spacer(Modifier.height(14.dp))
            AuthErrorBanner(message = errorMessage ?: "")
          }

          Spacer(Modifier.height(12.dp))
          Text(
            text = "Sign in with your Microsoft / STI email & password",
            color = RoyalNavy.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
          )
        }
      }
    }

    AuthStep.ADMIN_LOGIN -> {
      // Canva Page 17: AssiSTI Sta. Mesa badge + Admin ID + Password + Royal Blue Log In + Bottom-left STI logo
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .imePadding(),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          IconButton(
            onClick = {
              focusManager.clearFocus()
              errorMessage = null
              isLoading = false
              step = AuthStep.PORTAL_SELECT
            },
            modifier = Modifier
              .align(Alignment.CenterStart)
              .minimumInteractiveComponentSize()
              .testTag("admin_login_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to portal selection",
              tint = PureWhite,
            )
          }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 500.dp)
            .align(Alignment.CenterHorizontally)
            .padding(horizontal = 32.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          AssiSTIBoxBadge(badgeSize = 160.dp, showStaMesaLabel = true)
          Spacer(Modifier.height(30.dp))

          PillAuthField(
            value = adminId,
            onValueChange = {
              adminId = it
              errorMessage = null
            },
            placeholder = "Admin ID",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            testTag = "admin_id_input",
          )
          Spacer(Modifier.height(14.dp))
          PillPasswordField(
            value = adminPassword,
            onValueChange = {
              adminPassword = it
              errorMessage = null
            },
            placeholder = "Password",
            onDone = { submitAdmin() },
            testTag = "admin_password_input",
          )

          Spacer(Modifier.height(18.dp))

          Surface(
            color = DeepCircleBlue,
            shape = RoundedCornerShape(50),
            shadowElevation = 8.dp,
            modifier = Modifier
              .fillMaxWidth(0.86f)
              .height(74.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Button(
                onClick = { submitAdmin() },
                enabled = !isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MintPill,
                  contentColor = RoyalNavy,
                ),
                modifier = Modifier
                  .fillMaxWidth(0.76f)
                  .height(48.dp)
                  .minimumInteractiveComponentSize()
                  .testTag("admin_login_submit_button"),
              ) {
                if (isLoading) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = RoyalNavy,
                    strokeWidth = 2.dp,
                  )
                } else {
                  Text(
                    text = "Log In",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                  )
                }
              }
            }
          }

          if (errorMessage != null) {
            Spacer(Modifier.height(14.dp))
            AuthErrorBanner(message = errorMessage ?: "")
          }
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, bottom = 16.dp),
        ) {
          Image(
            painter = painterResource(id = R.drawable.sti_logo_transparent),
            contentDescription = "STI College Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .align(Alignment.CenterStart)
              .size(width = 112.dp, height = 66.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun AuthErrorBanner(message: String) {
  Surface(
    color = Color(0xFFFDECEC),
    border = BorderStroke(1.dp, ErrorRed),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = message,
      color = ErrorRed,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
    )
  }
}

@Composable
private fun PillAuthField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  keyboardType: KeyboardType,
  imeAction: ImeAction,
  onImeAction: () -> Unit,
  testTag: String,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = keyboardType,
      imeAction = imeAction,
    ),
    keyboardActions = KeyboardActions(
      onNext = { onImeAction() },
      onDone = { onImeAction() },
    ),
    placeholder = {
      Text(
        text = placeholder,
        color = MutedInk.copy(alpha = 0.65f),
        fontWeight = FontWeight.SemiBold,
      )
    },
    shape = RoundedCornerShape(50),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = PureWhite,
      unfocusedContainerColor = PureWhite,
      focusedBorderColor = RoyalNavy,
      unfocusedBorderColor = PureWhite,
      focusedTextColor = RoyalNavy,
      unfocusedTextColor = RoyalNavy,
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
  )
}

@Composable
private fun PillPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  onDone: () -> Unit,
  testTag: String,
) {
  var passwordVisible by remember { mutableStateOf(false) }

  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Password,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(onDone = { onDone() }),
    placeholder = {
      Text(
        text = placeholder,
        color = MutedInk.copy(alpha = 0.65f),
        fontWeight = FontWeight.SemiBold,
      )
    },
    trailingIcon = {
      IconButton(
        onClick = { passwordVisible = !passwordVisible },
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        Icon(
          imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
          contentDescription = if (passwordVisible) "Hide password" else "Show password",
          tint = MutedInk,
        )
      }
    },
    shape = RoundedCornerShape(50),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = PureWhite,
      unfocusedContainerColor = PureWhite,
      focusedBorderColor = RoyalNavy,
      unfocusedBorderColor = PureWhite,
      focusedTextColor = RoyalNavy,
      unfocusedTextColor = RoyalNavy,
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
  )
}
