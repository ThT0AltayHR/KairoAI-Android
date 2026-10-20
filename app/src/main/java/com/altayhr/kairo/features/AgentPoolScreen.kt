package com.altayhr.kairo.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.data.SecureApiKeyStore

private val PoolInk=Color(0xFF090A10); private val PoolPanel=Color(0xFF12141D); private val PoolText=Color(0xFFF4F5F7); private val PoolMuted=Color(0xFF9DA5B5); private val PoolAccent=Color(0xFFAA9BFF); private val PoolGreen=Color(0xFF66D6C0); private val PoolStroke=Color(0xFF2B3040)
private val roles=listOf("Sohbet ajanı","Planlama ajanı","Kodlama ajanı","Terminal yöneticisi","Hata ayıklama ajanı","Entegrasyon ajanı","Görev dağıtıcı ana ajan","Dinamik kapsül ve widget ajanı")
@Composable fun AgentPoolScreen() {
 val context=LocalContext.current; val store= remember{SecureApiKeyStore(context)}; val prefs= remember{context.getSharedPreferences("kairo_agent_pool",0)}
 var values by remember { mutableStateOf((1..8).map { store.getSecret("gemini_agent_key_$it").orEmpty() }) }; var shown by remember{mutableStateOf(-1)}; var selected by remember{mutableStateOf((1..8).associateWith{prefs.getString("role_$it",roles[it-1])?:roles[it-1]})}; var note by remember{mutableStateOf("Her ajan için ayrı anahtar atanabilir; aynı anahtarın birden fazla rolde kullanılması da mümkündür.")}
 Column(Modifier.fillMaxSize().background(PoolInk).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
  Text("Ajan filosu",color=PoolText,fontSize=26.sp,fontWeight=FontWeight.Bold); Text("8 Gemini anahtar yuvası · rol bazlı yönlendirme",color=PoolMuted,fontSize=11.sp)
  Card(colors=CardDefaults.cardColors(containerColor=PoolPanel),border=BorderStroke(1.dp,PoolStroke),shape=RoundedCornerShape(16.dp)) { Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) { Text("Ana ajan koordinasyonu",color=PoolText,fontWeight=FontWeight.SemiBold); Text("Ana ajan görev planı ve alt ajan istemlerini hazırlar; sohbet yanıtı ayrı tutulur. Bu ekranda anahtarlar güvenli saklanır. Gerçek paralel ajan yürütümü ve terminal ajanı terminal entegrasyonu aşamasına bağlıdır.",color=PoolMuted,fontSize=11.sp,lineHeight=16.sp) } }
  Text(note,color=PoolMuted,fontSize=11.sp)
  LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)) { items(8) { idx -> val slot=idx+1; Card(colors=CardDefaults.cardColors(containerColor=PoolPanel),border=BorderStroke(1.dp,PoolStroke),shape=RoundedCornerShape(15.dp)) { Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(30.dp).background(PoolAccent.copy(alpha=.15f),RoundedCornerShape(9.dp)),contentAlignment=Alignment.Center){Text("%02d".format(slot),color=PoolAccent,fontSize=10.sp,fontWeight=FontWeight.Bold)}; Column(Modifier.weight(1f).padding(start=9.dp)){Text(roles[idx],color=PoolText,fontWeight=FontWeight.SemiBold,fontSize=13.sp);Text(if(values[idx].isBlank())"Anahtar eklenmedi" else "Anahtar güvenli kasada",color=if(values[idx].isBlank())PoolMuted else PoolGreen,fontSize=10.sp)}; IconButton(onClick={shown=if(shown==slot)-1 else slot}) {Icon(if(shown==slot)Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,"Anahtar alanını göster",tint=PoolMuted)} }
   OutlinedTextField(value=values[idx],onValueChange={v->values=values.toMutableList().also{it[idx]=v}},modifier=Modifier.fillMaxWidth(),label={Text("Gemini API key $slot")},singleLine=true,visualTransformation=if(shown==slot)VisualTransformation.None else PasswordVisualTransformation(),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=PoolAccent,unfocusedBorderColor=PoolStroke,focusedTextColor=PoolText,unfocusedTextColor=PoolText,focusedLabelColor=PoolAccent,unfocusedLabelColor=PoolMuted))
   Row(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically) { Button(onClick={if(values[idx].isBlank()){store.clearSecret("gemini_agent_key_$slot");note="$slot numaralı anahtar silindi."}else{store.saveSecret("gemini_agent_key_$slot",values[idx]);note="$slot numaralı anahtar şifreli olarak kaydedildi."}},colors=ButtonDefaults.buttonColors(containerColor=PoolAccent,contentColor=PoolInk)) {Icon(Icons.Outlined.Save,null);Spacer(Modifier.width(4.dp));Text("Kaydet")}; OutlinedButton(onClick={values=values.toMutableList().also{it[idx]=""};store.clearSecret("gemini_agent_key_$slot");note="Anahtar silindi."}){Text("Sil")} }
  } } } }
 }
}
