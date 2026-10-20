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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.data.SecureApiKeyStore

private val VaultInk=Color(0xFF090A10); private val VaultPanel=Color(0xFF12141D); private val VaultText=Color(0xFFF4F5F7); private val VaultMuted=Color(0xFF9DA5B5); private val VaultAccent=Color(0xFFAA9BFF); private val VaultStroke=Color(0xFF2B3040)

@Composable
fun SecureVaultScreen() {
    val context= LocalContext.current
    val store= remember { SecureApiKeyStore(context) }
    val prefs= remember { context.getSharedPreferences("kairo_vault_index",0) }
    var entries by remember { mutableStateOf(loadVaultIndex(prefs.getString("entries", "[]").orEmpty())) }
    var label by remember { mutableStateOf("") }; var value by remember { mutableStateOf("") }; var reveal by remember { mutableStateOf(false) }; var note by remember { mutableStateOf("Kasadaki sırlar bu cihazda Android Keystore anahtarıyla şifrelenir.") }
    fun persist() { prefs.edit().putString("entries", org.json.JSONArray().also { a->entries.forEach { a.put(org.json.JSONObject().put("id",it.first).put("label",it.second)) } }.toString()).apply() }
    Column(Modifier.fillMaxSize().background(VaultInk).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Güvenli kasa",color=VaultText,fontSize=26.sp,fontWeight=FontWeight.Bold)
        Text("Tüm sohbetler ve projeler için ortak kasa · değerler sohbet geçmişine eklenmez",color=VaultMuted,fontSize=11.sp,lineHeight=16.sp)
        Card(colors=CardDefaults.cardColors(containerColor=VaultPanel),border=BorderStroke(1.dp,VaultStroke),shape=RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(value=label,onValueChange={label=it},modifier=Modifier.fillMaxWidth(),label={Text("Kasa adı (örn. GITHUB_TOKEN)")},singleLine=true,colors=vaultColors())
                OutlinedTextField(value=value,onValueChange={value=it},modifier=Modifier.fillMaxWidth(),label={Text("Değer / token / API anahtarı")},singleLine=true,visualTransformation=if(reveal) VisualTransformation.None else PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),trailingIcon={IconButton(onClick={reveal=!reveal}) { Icon(if(reveal)Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,if(reveal)"Değeri gizle" else "Değeri göster",tint=VaultMuted) }},colors=vaultColors())
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Button(onClick={
                        val clean=label.trim().uppercase().replace(Regex("[^A-Z0-9_]+"),"_").take(55)
                        if(clean.isBlank()||value.isBlank()) note="Ad ve değer boş olamaz."
                        else { store.saveSecret("vault_$clean",value); entries=(entries.filterNot{it.first==clean}+ (clean to clean)); persist(); label=""; value=""; reveal=false; note="Kasa öğesi güvenli şekilde kaydedildi." }
                    },modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=VaultAccent,contentColor=VaultInk)) { Icon(Icons.Outlined.Save,null); Spacer(Modifier.width(6.dp)); Text("Kaydet") }
                    OutlinedButton(onClick={label="";value="";reveal=false},modifier=Modifier.weight(1f)) { Icon(Icons.Outlined.Close,null); Spacer(Modifier.width(6.dp)); Text("İptal") }
                }
            }
        }
        Text(note,color=VaultMuted,fontSize=11.sp)
        Text("KAYITLI SIRLAR",color=VaultMuted,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            items(entries.size,key={entries[it].first}) { idx -> val item=entries[idx]; var show by remember(item.first){mutableStateOf(false)}
                Card(colors=CardDefaults.cardColors(containerColor=VaultPanel),border=BorderStroke(1.dp,VaultStroke),shape=RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.second,color=VaultText,fontWeight=FontWeight.SemiBold,fontSize=13.sp); Text("${if(store.getSecret("vault_${item.first}")!=null)"Değer kayıtlı" else "Değer bulunamadı"}",color=VaultMuted,fontSize=10.sp) }; IconButton(onClick={show=!show}) { Icon(if(show)Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,"Değeri göster/gizle",tint=VaultMuted) }; IconButton(onClick={store.clearSecret("vault_${item.first}");entries=entries.filterNot{it.first==item.first};persist();note="Kasa öğesi silindi."}) { Icon(Icons.Outlined.DeleteOutline,"Kasadan sil",tint=Color(0xFFFF7D88)) } }
                        if(show) Text(store.getSecret("vault_${item.first}") ?: "Şifre çözülemedi veya kayıt yok",color=VaultAccent,fontSize=11.sp)
                    }
                }
            }
            if(entries.isEmpty()) item { Text("Henüz ortak kasa öğesi yok. Tokenları burada isimlendirip kaydet.",color=VaultMuted,fontSize=12.sp) }
        }
        Text("Güvenlik sınırı: kasa değerlerini uygulama ekranları dışında otomatik olarak terminal komutlarına enjekte eden entegrasyon henüz etkin değil. Değerler günlüklerde veya Gemini istemlerine eklenmez.",color=VaultMuted,fontSize=10.sp,lineHeight=15.sp)
    }
}

private fun loadVaultIndex(raw:String):List<Pair<String,String>> = runCatching { val a=org.json.JSONArray(raw); (0 until a.length()).mapNotNull { i->a.optJSONObject(i)?.let { it.optString("id") to it.optString("label") } } }.getOrDefault(emptyList())
@Composable private fun vaultColors()=OutlinedTextFieldDefaults.colors(focusedBorderColor=VaultAccent,unfocusedBorderColor=VaultStroke,focusedTextColor=VaultText,unfocusedTextColor=VaultText,focusedLabelColor=VaultAccent,unfocusedLabelColor=VaultMuted,cursorColor=VaultAccent)
