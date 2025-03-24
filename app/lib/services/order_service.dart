import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/order.dart';

class OrderService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  Future<void> saveOrder(Order order) async {
    try {
      await _firestore.collection('orders').doc(order.id).set(order.toMap());
    } catch (e) {
      print('Error saving order: $e');
      throw Exception('Failed to save order: $e');
    }
  }

  Future<List<Order>> getUserOrders(String userId) async {
    try {
      final QuerySnapshot snapshot = await _firestore
          .collection('orders')
          .where('userId', isEqualTo: userId)
          .orderBy('createdAt', descending: true)
          .get();

      return snapshot.docs
          .map((doc) => Order.fromMap(doc.data() as Map<String, dynamic>))
          .toList();
    } catch (e) {
      print('Error fetching orders: $e');
      throw Exception('Failed to fetch orders: $e');
    }
  }

  Future<Order?> getOrder(String orderId) async {
    try {
      final DocumentSnapshot doc =
          await _firestore.collection('orders').doc(orderId).get();
      
      if (!doc.exists) return null;
      
      return Order.fromMap(doc.data() as Map<String, dynamic>);
    } catch (e) {
      print('Error fetching order: $e');
      throw Exception('Failed to fetch order: $e');
    }
  }
} 