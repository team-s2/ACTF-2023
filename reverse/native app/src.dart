import 'dart:ffi';

import 'package:flutter/material.dart';
import 'dart:convert';
import 'enc.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Demo',
      home: LongPressDemo(),
    );
  }
}

class LongPressDemo extends StatefulWidget {
  @override
  _LongPressDemoState createState() => _LongPressDemoState();
}
// 
// Iu2xpwXLAK734btEt9kXIhfpRgTlu6KuI0
class _LongPressDemoState extends State<LongPressDemo> {
  String _inputText = '';
  String _outputText = '';
  var inputByte = [0];
  var encByte = [0];
  var key = [140, 136, 210, 238, 167, 102, 222, 38,];
  var cmp = [184,132,137,215,146,65,86,157,123,100,179,131,112,170,97,210,163,179,17,171,245,30,194,144,37,41,235,121,146,210,174,92,204,22];
  void _onTap(){
    encByte = rc4Encrypt(inputByte, key);
    setState(() {
        _outputText = 'submitted:$_inputText';
      });
  }
  void _onLongPressed() {
    if (cmp.length != encByte.length) return;
    for (int i = 0; i < cmp.length; i++) {
      if (cmp[i] != encByte[i]) {
        setState(() {
          _outputText = 'false';
        });
        return ;
      }
    }
    setState(() {
      _outputText = 'true';
    });
    
  }

  void _onChanged(String value) {
      setState(() {
        _outputText = value;
      });
  }
  void _onSubmit(String value){
      _inputText = value;
      inputByte = utf8.encode(value);
      for (var i = 0; i < inputByte.length; i++) {
        inputByte[i] ^= 0xff;
      }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Chall'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            TextField(
              onChanged: _onChanged,
              onSubmitted: _onSubmit,
              decoration: InputDecoration(
                hintText: '请输入文本',
              ),
            ),
            SizedBox(height: 20),
            GestureDetector(
              onLongPress: _onLongPressed,
              onTap: _onTap,
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                color: Colors.blue,
                child: Text(
                  '提交',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                  ),
                ),
              ),
            ),
            SizedBox(height: 20),
            Text(_outputText),
          ],
        ),
      ),
    );
  }
}