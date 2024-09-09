package com.example.mylogin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mylogin.ui.theme.MyloginTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyloginTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var token by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("تسجيل الدخول")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("اسم المستخدم") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            login(username.text, password.text) { receivedToken ->
                token = receivedToken
                Log.d("LoginActivity", "Received Token: $token") // طباعة التوكن الفعلي
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("تسجيل الدخول")
        }
    }
}

private fun login(username: String, password: String, onResult: (String) -> Unit) {
    val client = OkHttpClient()
    val json = """{"username": "$username", "password": "$password"}"""
    val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)

    val request = Request.Builder()
        .url("https://dummyjson.com/auth/login")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult("") // إرجاع توكن فارغ في حالة الفشل
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val token = responseBody?.let { parseToken(it) } ?: ""
                Log.d("LoginActivity", "Received Token: $token") // طباعة التوكن الفعلي
                onResult(token)
            } else {
                Log.e("LoginActivity", "Login failed: ${response.message}") // طباعة رسالة الخطأ
                onResult("") // إرجاع توكن فارغ في حالة الفشل
            }
        }
    })
}

private fun parseToken(responseBody: String): String {
    // تحليل الاستجابة JSON
    val jsonObject = JSONObject(responseBody)
    return jsonObject.optString("token", "") // استبدل "token" بالمفتاح الصحيح من استجابة الخادم
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyloginTheme {
        LoginScreen()
    }
}