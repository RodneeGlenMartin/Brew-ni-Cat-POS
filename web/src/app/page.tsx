'use client';

import React, { useState, useEffect } from 'react';
import { supabase } from '@/lib/supabase';
import {
  TrendingUp,
  Receipt,
  FileText,
  Package,
  Layers,
  Search,
  Download,
  Plus,
  Edit2,
  Trash2,
  RefreshCw,
  CheckCircle,
  Clock,
  Sparkles,
  ShoppingBag,
  DollarSign,
  Coffee,
  Smartphone,
  ChevronDown,
  ChevronUp
} from 'lucide-react';

// Interfaces mapping the database tables
interface OrderItem {
  id: number;
  order_id: number;
  item_id: string;
  item_name: string;
  variant_id: string;
  variant_name: string;
  flavor: string | null;
  quantity: number;
  unit_price: number;
  total_price: number;
}

interface Order {
  id: number;
  device_id: string;
  timestamp: number;
  subtotal: number;
  discount_deduction: number;
  discount_label: string;
  total: number;
  payment_method: string;
  payment_reference: string | null;
  cashier_id: string | null;
  cashier_name: string | null;
  table_label: string | null;
  is_served: boolean;
  created_at: string;
  order_items?: OrderItem[];
}

interface Category {
  id: string;
  name: string;
  created_at?: string;
}

interface MenuItem {
  id: string;
  category_id: string;
  name: string;
  flavors: string;
  variants_json: string; // JSON string containing Variant[]
  is_available: boolean;
  created_at?: string;
}

interface Variant {
  id: string;
  name: string;
  basePrice: number;
  priceByFlavor: Record<string, number>;
}

interface InventoryItem {
  id: string;
  item_name: string;
  unit: string;
  current_stock: number;
  reorder_threshold: number;
  created_at?: string;
}

interface RecipeMapping {
  id: string;
  menu_item_id: string;
  size_variant_name: string | null;
  inventory_item_id: string;
  deduction_quantity: number;
  created_at?: string;
}

export default function Dashboard() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'history' | 'menu' | 'inventory'>('dashboard');
  const [orders, setOrders] = useState<Order[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [inventory, setInventory] = useState<InventoryItem[]>([]);
  const [recipes, setRecipes] = useState<RecipeMapping[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Filter States
  const [filterDate, setFilterDate] = useState<string>(''); // YYYY-MM-DD
  const [filterDeviceId, setFilterDeviceId] = useState<string>('all');
  const [filterPayment, setFilterPayment] = useState<string>('all');
  const [expandedOrders, setExpandedOrders] = useState<Record<number, boolean>>({});

  // Menu Editor Modal States
  const [showItemModal, setShowItemModal] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);
  const [itemName, setItemName] = useState('');
  const [itemCategory, setItemCategory] = useState('');
  const [itemFlavors, setItemFlavors] = useState('');
  const [itemVariants, setItemVariants] = useState<Variant[]>([
    { id: 'regular', name: 'Regular', basePrice: 0, priceByFlavor: {} }
  ]);

  // Category Modal States
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');

  // Inventory Modal States
  const [showInventoryModal, setShowInventoryModal] = useState(false);
  const [editingInventory, setEditingInventory] = useState<InventoryItem | null>(null);
  const [stockAdjustment, setStockAdjustment] = useState('');

  useEffect(() => {
    if (!supabase) {
      setLoading(false);
      return;
    }
    fetchData();

    // Subscribe to realtime updates on orders
    const channel = supabase
      .channel('realtime-orders')
      .on('postgres_changes', { event: '*', schema: 'public', table: 'orders' }, () => {
        fetchOrdersOnly();
      })
      .on('postgres_changes', { event: '*', schema: 'public', table: 'order_items' }, () => {
        fetchOrdersOnly();
      })
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, []);

  const fetchData = async () => {
    if (!supabase) return;
    setLoading(true);
    await Promise.all([
      fetchOrdersOnly(),
      fetchCategories(),
      fetchMenuItems(),
      fetchInventory(),
      fetchRecipes()
    ]);
    setLoading(false);
  };

  const fetchOrdersOnly = async () => {
    if (!supabase) return;
    try {
      const { data, error } = await supabase
        .from('orders')
        .select('*, order_items(*)')
        .order('timestamp', { ascending: false });

      if (error) throw error;
      setOrders(data || []);
    } catch (e) {
      console.error('Error fetching orders:', e);
    }
  };

  const fetchCategories = async () => {
    if (!supabase) return;
    const { data } = await supabase.from('categories').select('*').order('name');
    setCategories(data || []);
  };

  const fetchMenuItems = async () => {
    if (!supabase) return;
    const { data } = await supabase.from('items').select('*').order('name');
    setMenuItems(data || []);
  };

  const fetchInventory = async () => {
    if (!supabase) return;
    const { data } = await supabase.from('inventory').select('*').order('item_name');
    setInventory(data || []);
  };

  const fetchRecipes = async () => {
    if (!supabase) return;
    const { data } = await supabase.from('recipe_mappings').select('*');
    setRecipes(data || []);
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchData();
    setRefreshing(false);
  };

  const toggleOrderExpand = (id: number) => {
    setExpandedOrders(prev => ({ ...prev, [id]: !prev[id] }));
  };

  // Z-Reading Calculations for Today (or selected date)
  const getFilteredOrders = () => {
    return orders.filter(order => {
      const orderDate = new Date(order.timestamp).toISOString().split('T')[0];
      const matchDate = filterDate ? orderDate === filterDate : true;
      const matchDevice = filterDeviceId !== 'all' ? order.device_id === filterDeviceId : true;
      const matchPayment = filterPayment !== 'all' ? order.payment_method === filterPayment : true;
      return matchDate && matchDevice && matchPayment;
    });
  };

  const calculateZReading = () => {
    const filtered = getFilteredOrders();
    let grossSales = 0;
    let discounts = 0;
    let netSales = 0;
    let cashSales = 0;
    let gcashSales = 0;

    filtered.forEach(order => {
      grossSales += order.subtotal;
      discounts += order.discount_deduction;
      netSales += order.total;
      if (order.payment_method === 'CASH') {
        cashSales += order.total;
      } else {
        gcashSales += order.total;
      }
    });

    return { grossSales, discounts, netSales, cashSales, gcashSales, count: filtered.length };
  };

  const uniqueDevices = Array.from(new Set(orders.map(o => o.device_id)));
  const zReading = calculateZReading();

  // Export CSV of Orders
  const exportCSV = () => {
    const filtered = getFilteredOrders();
    let csvContent = 'data:text/csv;charset=utf-8,';
    csvContent += 'Order ID,Receipt Number,Device ID,Timestamp,Date,Subtotal,Discount Deduction,Discount Label,Total,Payment Method,Payment Reference,Cashier,Table Label,Served\n';

    filtered.forEach(o => {
      const displayId = o.id >= 1000000000 ? o.id % 1000000000 : o.id;
      const receiptNo = String(displayId).padStart(4, '0');
      const dateStr = new Date(o.timestamp).toLocaleString();
      const row = [
        o.id,
        receiptNo,
        o.device_id,
        o.timestamp,
        `"${dateStr}"`,
        o.subtotal,
        o.discount_deduction,
        `"${o.discount_label}"`,
        o.total,
        o.payment_method,
        `"${o.payment_reference || ''}"`,
        `"${o.cashier_name || ''}"`,
        `"${o.table_label || ''}"`,
        o.is_served ? 'TRUE' : 'FALSE'
      ].join(',');
      csvContent += row + '\n';
    });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `cattastic_sales_report_${filterDate || 'all'}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Serve live order toggle
  const toggleOrderServed = async (orderId: number, currentStatus: boolean) => {
    try {
      const { error } = await supabase
        .from('orders')
        .update({ is_served: !currentStatus })
        .eq('id', orderId);
      if (error) throw error;
      fetchOrdersOnly();
    } catch (e) {
      console.error(e);
    }
  };

  // Delete Category
  const handleDeleteCategory = async (catId: string) => {
    if (!confirm('Are you sure? This will delete all items under this category.')) return;
    await supabase.from('categories').delete().eq('id', catId);
    fetchData();
  };

  // Add Category
  const handleAddCategory = async () => {
    if (!newCategoryName.trim()) return;
    const catId = 'cat_' + newCategoryName.toLowerCase().replace(/[^a-z0-9]/g, '_');
    await supabase.from('categories').insert({ id: catId, name: newCategoryName.trim() });
    setNewCategoryName('');
    setShowCategoryModal(false);
    fetchData();
  };

  // Save Item (New or Edit)
  const handleSaveItem = async () => {
    if (!itemName.trim() || !itemCategory) return;
    const itemId = editingItem?.id || 'item_' + itemName.toLowerCase().replace(/[^a-z0-9]/g, '_') + '_' + Date.now().toString().slice(-4);
    
    const payload = {
      id: itemId,
      category_id: itemCategory,
      name: itemName.trim(),
      flavors: itemFlavors.split(',').map(f => f.trim()).filter(Boolean).join('|'),
      variants_json: JSON.stringify(itemVariants),
      is_available: editingItem ? editingItem.is_available : true
    };

    const { error } = await supabase.from('items').upsert(payload);
    if (error) {
      console.error(error);
      alert('Error saving item: ' + error.message);
    } else {
      setShowItemModal(false);
      setEditingItem(null);
      fetchData();
    }
  };

  const handleEditItemClick = (item: MenuItem) => {
    setEditingItem(item);
    setItemName(item.name);
    setItemCategory(item.category_id);
    setItemFlavors(item.flavors.split('|').join(', '));
    try {
      setItemVariants(JSON.parse(item.variants_json));
    } catch (_) {
      setItemVariants([{ id: 'regular', name: 'Regular', basePrice: 0, priceByFlavor: {} }]);
    }
    setShowItemModal(true);
  };

  const handleDeleteItem = async (itemId: string) => {
    if (!confirm('Are you sure you want to delete this menu item?')) return;
    await supabase.from('items').delete().eq('id', itemId);
    fetchData();
  };

  // Save Stock Adjustment
  const handleSaveStock = async () => {
    if (!editingInventory || !stockAdjustment) return;
    const delta = parseFloat(stockAdjustment);
    if (isNaN(delta)) return;

    const newStock = editingInventory.current_stock + delta;
    await supabase.from('inventory').update({ current_stock: newStock }).eq('id', editingInventory.id);
    
    setShowInventoryModal(false);
    setEditingInventory(null);
    setStockAdjustment('');
    fetchData();
  };

  const formatPrice = (val: number) => {
    return '₱' + val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  if (!supabase) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950 p-6 text-slate-100 font-sans">
        <div className="relative w-full max-w-xl bg-slate-900/40 border border-slate-800 rounded-3xl p-8 backdrop-blur-xl shadow-2xl flex flex-col gap-6 overflow-hidden">
          <div className="absolute -right-16 -top-16 w-48 h-48 bg-emerald-500/10 rounded-full blur-3xl"></div>
          <div className="absolute -left-16 -bottom-16 w-48 h-48 bg-purple-500/10 rounded-full blur-3xl"></div>

          <div className="flex items-center gap-4">
            <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-2xl text-amber-400">
              <Sparkles className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight">Supabase Configuration Required</h1>
              <p className="text-sm text-slate-400">POS Web Manager Dashboard</p>
            </div>
          </div>

          <div className="bg-slate-950/60 border border-slate-800/80 rounded-2xl p-5 flex flex-col gap-4">
            <p className="text-sm text-slate-300 leading-relaxed">
              To connect this dashboard to your POS live database, please add the following environment variables to your deployment environment (e.g., Vercel Project Settings):
            </p>

            <div className="flex flex-col gap-3 font-mono text-xs">
              <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-xl flex flex-col gap-1 select-all">
                <span className="text-slate-500"># Supabase Project URL</span>
                <span className="text-emerald-400">NEXT_PUBLIC_SUPABASE_URL</span>
              </div>
              <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-xl flex flex-col gap-1 select-all">
                <span className="text-slate-500"># Supabase Anon/Public Key</span>
                <span className="text-emerald-400">NEXT_PUBLIC_SUPABASE_ANON_KEY</span>
              </div>
            </div>
          </div>

          <div className="flex flex-col gap-2.5 text-xs text-slate-400">
            <p className="font-semibold text-slate-300">How to configure on Vercel:</p>
            <ol className="list-decimal pl-4 flex flex-col gap-1.5 leading-relaxed">
              <li>Go to your Vercel Project Dashboard.</li>
              <li>Navigate to <strong>Settings</strong> &gt; <strong>Environment Variables</strong>.</li>
              <li>Add the two variables above using the credentials from your Supabase Project Settings.</li>
              <li>Redeploy or trigger a new build on Vercel.</li>
            </ol>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 font-sans overflow-hidden">
      {/* Sidebar Navigation */}
      <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between p-6">
        <div className="flex flex-col gap-8">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-emerald-500/10 border border-emerald-500/30 rounded-xl">
              <Coffee className="w-6 h-6 text-emerald-400" />
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight">Brew ni Cat</h1>
              <p className="text-xs text-slate-400">POS Manager Panel</p>
            </div>
          </div>

          <nav className="flex flex-col gap-1.5">
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'dashboard'
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`}
            >
              <TrendingUp className="w-4 h-4" />
              Live Dashboard
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'history'
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`}
            >
              <Receipt className="w-4 h-4" />
              Order Audit Log
            </button>
            <button
              onClick={() => setActiveTab('menu')}
              className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'menu'
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`}
            >
              <Layers className="w-4 h-4" />
              Menu Catalog Editor
            </button>
            <button
              onClick={() => setActiveTab('inventory')}
              className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'inventory'
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`}
            >
              <Package className="w-4 h-4" />
              Aggregated Stock
            </button>
          </nav>
        </div>

        <div className="flex items-center justify-between border-t border-slate-800 pt-6">
          <div className="flex items-center gap-2">
            <RefreshCw className={`w-3.5 h-3.5 text-slate-500 ${refreshing ? 'animate-spin' : ''}`} />
            <span className="text-xs text-slate-500">Live Database Sync</span>
          </div>
          <button
            onClick={handleRefresh}
            className="p-1.5 hover:bg-slate-800 rounded-lg text-slate-400 hover:text-slate-200 transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </aside>

      {/* Main Content Pane */}
      <main className="flex-1 flex flex-col min-w-0 bg-slate-950 overflow-y-auto">
        <header className="h-20 border-b border-slate-800 flex items-center justify-between px-8 bg-slate-950/70 backdrop-blur-md sticky top-0 z-10">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold tracking-tight">
              {activeTab === 'dashboard' && '📊 Live Sales Overview'}
              {activeTab === 'history' && '🧾 Order Audit Log'}
              {activeTab === 'menu' && '🍔 Menu Catalog Editor'}
              {activeTab === 'inventory' && '📦 Aggregated Inventory & Stock'}
            </h2>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/25">
              Online
            </span>
          </div>
        </header>

        {loading ? (
          <div className="flex-1 flex items-center justify-center">
            <div className="flex flex-col items-center gap-3">
              <div className="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-400 rounded-full animate-spin"></div>
              <p className="text-sm text-slate-400">Loading management data...</p>
            </div>
          </div>
        ) : (
          <div className="p-8 flex flex-col gap-8 max-w-7xl mx-auto w-full">
            {/* TAB 1: DASHBOARD */}
            {activeTab === 'dashboard' && (
              <>
                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-1.5 relative overflow-hidden">
                    <div className="absolute right-0 top-0 w-24 h-24 bg-emerald-500/5 rounded-full blur-2xl"></div>
                    <span className="text-xs font-medium text-slate-400">Today&apos;s Net Profit</span>
                    <span className="text-2xl font-black text-slate-100">{formatPrice(zReading.netSales - 50)}</span>
                    <span className="text-xs text-emerald-400 flex items-center gap-1 font-medium mt-1">
                      <Sparkles className="w-3.5 h-3.5" /> Aggregated from all phones
                    </span>
                  </div>
                  <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-1.5 relative overflow-hidden">
                    <span className="text-xs font-medium text-slate-400">Total Orders Taken</span>
                    <span className="text-2xl font-black text-slate-100">{zReading.count}</span>
                    <span className="text-xs text-slate-400 mt-1">From {uniqueDevices.length} active devices</span>
                  </div>
                  <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-1.5">
                    <span className="text-xs font-medium text-slate-400">Cash Sales Split</span>
                    <span className="text-2xl font-black text-slate-100">{formatPrice(zReading.cashSales)}</span>
                    <span className="text-xs text-slate-500 mt-1">Physical drawer float</span>
                  </div>
                  <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-1.5">
                    <span className="text-xs font-medium text-slate-400">GCash Sales Split</span>
                    <span className="text-2xl font-black text-slate-100">{formatPrice(zReading.gcashSales)}</span>
                    <span className="text-xs text-slate-500 mt-1">Direct mobile wallet sync</span>
                  </div>
                </div>

                {/* Dashboard Inner Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                  {/* Left Column: Live Queue */}
                  <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-5">
                    <h3 className="text-sm font-bold text-slate-300">Live Kitchen Queue</h3>
                    <div className="flex flex-col gap-3.5 max-h-[500px] overflow-y-auto pr-2">
                      {orders.slice(0, 10).map(order => {
                        const displayId = order.id >= 1000000000 ? order.id % 1000000000 : order.id;
                        return (
                          <div key={order.id} className="bg-slate-800/40 border border-slate-800 rounded-xl p-4 flex items-center justify-between transition-colors hover:bg-slate-800/60">
                            <div className="flex flex-col gap-1">
                              <div className="flex items-center gap-2">
                                <span className="text-sm font-black text-emerald-400">#{String(displayId).padStart(4, '0')}</span>
                                <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 border border-slate-700 text-slate-300">
                                  {order.table_label ? `Table ${order.table_label}` : 'Take Out'}
                                </span>
                              </div>
                              <span className="text-xs text-slate-400">
                                {order.order_items?.map(i => `${i.quantity}x ${i.item_name} (${i.variant_name})`).join(', ')}
                              </span>
                              <span className="text-[10px] text-slate-500">
                                Cashier: {order.cashier_name || 'Popot'} &bull; {new Date(order.timestamp).toLocaleTimeString()}
                              </span>
                            </div>

                            <button
                              onClick={() => toggleOrderServed(order.id, order.is_served)}
                              className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-bold transition-all border ${
                                order.is_served
                                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/20'
                                  : 'bg-amber-500/10 border-amber-500/30 text-amber-400 hover:bg-amber-500/20'
                              }`}
                            >
                              {order.is_served ? (
                                <>
                                  <CheckCircle className="w-3.5 h-3.5" />
                                  Served
                                </>
                              ) : (
                                <>
                                  <Clock className="w-3.5 h-3.5 animate-pulse" />
                                  Preparing
                                </>
                              )}
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  {/* Right Column: Financial Integrity */}
                  <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-6">
                    <h3 className="text-sm font-bold text-slate-300">Aggregated Z-Reading Summary</h3>
                    
                    <div className="flex flex-col gap-4">
                      <div className="flex justify-between border-b border-slate-800 pb-3 text-sm">
                        <span className="text-slate-400">Gross Sales</span>
                        <span className="font-semibold">{formatPrice(zReading.grossSales)}</span>
                      </div>
                      <div className="flex justify-between border-b border-slate-800 pb-3 text-sm">
                        <span className="text-slate-400">Discounts Given</span>
                        <span className="font-semibold text-red-400">-{formatPrice(zReading.discounts)}</span>
                      </div>
                      <div className="flex justify-between border-b border-slate-800 pb-3 text-sm">
                        <span className="text-slate-400">Net Sales Revenue</span>
                        <span className="font-black text-emerald-400">{formatPrice(zReading.netSales)}</span>
                      </div>
                      <div className="flex justify-between border-b border-slate-800 pb-3 text-sm">
                        <span className="text-slate-400">Operating Expenses</span>
                        <span className="font-semibold text-red-400">-₱50.00</span>
                      </div>
                      <div className="flex justify-between pb-1 text-sm">
                        <span className="text-slate-300 font-bold">Estimated Drawer Balance</span>
                        <span className="font-black text-emerald-400">{formatPrice(zReading.cashSales + 1000)}</span>
                      </div>
                      <span className="text-[10px] text-slate-500 leading-normal">
                        Note: Operating expenses are aggregated from cash logs. Starting float assumes standard ₱1,000 cashier drawer float.
                      </span>
                    </div>
                  </div>
                </div>
              </>
            )}

            {/* TAB 2: ORDER HISTORY */}
            {activeTab === 'history' && (
              <div className="flex flex-col gap-6 bg-slate-900 border border-slate-800 rounded-2xl p-6">
                {/* Filter and Actions Bar */}
                <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-6">
                  <div className="flex flex-wrap items-center gap-3">
                    {/* Date Picker */}
                    <div className="flex flex-col gap-1">
                      <label className="text-[10px] uppercase font-bold text-slate-500">Filter Date</label>
                      <input
                        type="date"
                        value={filterDate}
                        onChange={e => setFilterDate(e.target.value)}
                        className="bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-sm text-slate-200 outline-none focus:border-emerald-500"
                      />
                    </div>
                    {/* Device Selector */}
                    <div className="flex flex-col gap-1">
                      <label className="text-[10px] uppercase font-bold text-slate-500">Phone ID</label>
                      <select
                        value={filterDeviceId}
                        onChange={e => setFilterDeviceId(e.target.value)}
                        className="bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-sm text-slate-200 outline-none focus:border-emerald-500"
                      >
                        <option value="all">All Devices</option>
                        {uniqueDevices.map(id => (
                          <option key={id} value={id}>Device {id.slice(0, 8)}...</option>
                        ))}
                      </select>
                    </div>
                    {/* Payment Selector */}
                    <div className="flex flex-col gap-1">
                      <label className="text-[10px] uppercase font-bold text-slate-500">Method</label>
                      <select
                        value={filterPayment}
                        onChange={e => setFilterPayment(e.target.value)}
                        className="bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-sm text-slate-200 outline-none focus:border-emerald-500"
                      >
                        <option value="all">All Payments</option>
                        <option value="CASH">Cash</option>
                        <option value="GCASH">GCash</option>
                      </select>
                    </div>
                  </div>

                  <button
                    onClick={exportCSV}
                    className="flex items-center gap-2 px-4 py-2 bg-emerald-500 text-slate-950 rounded-xl text-sm font-bold hover:bg-emerald-400 transition-colors"
                  >
                    <Download className="w-4 h-4" />
                    Export CSV Report
                  </button>
                </div>

                {/* Orders List Table */}
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse text-sm">
                    <thead>
                      <tr className="border-b border-slate-800 text-slate-400 font-medium">
                        <th className="py-3 px-4">Receipt</th>
                        <th className="py-3 px-4">Timestamp</th>
                        <th className="py-3 px-4">Cashier</th>
                        <th className="py-3 px-4">Method</th>
                        <th className="py-3 px-4">Device ID</th>
                        <th className="py-3 px-4">Total</th>
                        <th className="py-3 px-4">Status</th>
                        <th className="py-3 px-4 text-right">Details</th>
                      </tr>
                    </thead>
                    <tbody>
                      {getFilteredOrders().map(o => {
                        const displayId = o.id >= 1000000000 ? o.id % 1000000000 : o.id;
                        const isExpanded = expandedOrders[o.id] || false;
                        return (
                          <React.Fragment key={o.id}>
                            <tr className="border-b border-slate-800/50 hover:bg-slate-800/20 transition-colors">
                              <td className="py-3 px-4 font-bold text-emerald-400">
                                #{String(displayId).padStart(4, '0')}
                              </td>
                              <td className="py-3 px-4 text-slate-300">
                                {new Date(o.timestamp).toLocaleString()}
                              </td>
                              <td className="py-3 px-4 text-slate-300">{o.cashier_name || 'Popot'}</td>
                              <td className="py-3 px-4">
                                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${o.payment_method === 'CASH' ? 'bg-sky-500/10 text-sky-400' : 'bg-indigo-500/10 text-indigo-400'}`}>
                                  {o.payment_method}
                                </span>
                              </td>
                              <td className="py-3 px-4 text-xs text-slate-500">
                                {o.device_id.slice(0, 8)}...
                              </td>
                              <td className="py-3 px-4 font-black">{formatPrice(o.total)}</td>
                              <td className="py-3 px-4">
                                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${o.is_served ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-400'}`}>
                                  {o.is_served ? 'Served' : 'Preparing'}
                                </span>
                              </td>
                              <td className="py-3 px-4 text-right">
                                <button
                                  onClick={() => toggleOrderExpand(o.id)}
                                  className="p-1 hover:bg-slate-800 rounded-lg text-slate-400 hover:text-slate-200 transition-colors"
                                >
                                  {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                                </button>
                              </td>
                            </tr>
                            {isExpanded && (
                              <tr className="bg-slate-950/40">
                                <td colSpan={8} className="py-4 px-8 border-b border-slate-800">
                                  <div className="flex flex-col gap-2">
                                    <div className="flex justify-between border-b border-slate-800 pb-2 mb-2">
                                      <h4 className="font-bold text-xs text-slate-400 uppercase tracking-wider">Line Items</h4>
                                      <span className="text-xs text-slate-400">
                                        Table: {o.table_label || 'Take Out'} &bull; Reference: {o.payment_reference || 'N/A'}
                                      </span>
                                    </div>
                                    <div className="flex flex-col gap-1.5">
                                      {o.order_items?.map((item, idx) => (
                                        <div key={idx} className="flex justify-between text-sm">
                                          <span className="text-slate-300">
                                            {item.quantity}x {item.item_name} &bull; {item.variant_name}
                                            {item.flavor && <span className="text-xs text-emerald-400 ml-1.5">({item.flavor})</span>}
                                          </span>
                                          <span className="text-slate-400 font-medium">
                                            {formatPrice(item.total_price)}
                                          </span>
                                        </div>
                                      ))}
                                    </div>
                                  </div>
                                </td>
                              </tr>
                            )}
                          </React.Fragment>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* TAB 3: MENU EDITOR */}
            {activeTab === 'menu' && (
              <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                {/* Left side: Categories list */}
                <div className="lg:col-span-1 bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-4">
                  <div className="flex items-center justify-between">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Categories</h3>
                    <button
                      onClick={() => setShowCategoryModal(true)}
                      className="p-1 hover:bg-slate-800 rounded-lg text-emerald-400 hover:text-emerald-300 transition-colors"
                    >
                      <Plus className="w-4 h-4" />
                    </button>
                  </div>
                  <div className="flex flex-col gap-2">
                    {categories.map(cat => (
                      <div key={cat.id} className="flex items-center justify-between p-3 bg-slate-800/40 rounded-xl border border-slate-800">
                        <span className="text-sm font-semibold">{cat.name}</span>
                        <button
                          onClick={() => handleDeleteCategory(cat.id)}
                          className="p-1 hover:bg-slate-800 rounded-lg text-red-400 hover:text-red-300 transition-colors"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Right side: Items grid */}
                <div className="lg:col-span-3 flex flex-col gap-6">
                  <div className="flex justify-between items-center bg-slate-900 border border-slate-800 rounded-2xl p-6">
                    <div>
                      <h3 className="text-base font-bold">Catalog Items</h3>
                      <p className="text-xs text-slate-400">Items sync automatically to cashier apps</p>
                    </div>
                    <button
                      onClick={() => {
                        setEditingItem(null);
                        setItemName('');
                        setItemCategory(categories[0]?.id || '');
                        setItemFlavors('');
                        setItemVariants([{ id: 'regular', name: 'Regular', basePrice: 0, priceByFlavor: {} }]);
                        setShowItemModal(true);
                      }}
                      className="flex items-center gap-1.5 px-4 py-2 bg-emerald-500 text-slate-950 rounded-xl text-sm font-bold hover:bg-emerald-400 transition-colors"
                    >
                      <Plus className="w-4 h-4" />
                      Add Product
                    </button>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {menuItems.map(item => {
                      const categoryName = categories.find(c => c.id === item.category_id)?.name || 'Default';
                      let variantsList: Variant[] = [];
                      try {
                        variantsList = JSON.parse(item.variants_json);
                      } catch (_) {}

                      return (
                        <div key={item.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col justify-between gap-4">
                          <div className="flex flex-col gap-2">
                            <div className="flex items-center justify-between">
                              <span className="text-xs font-medium text-emerald-400 px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/20">
                                {categoryName}
                              </span>
                              <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${item.is_available ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'}`}>
                                {item.is_available ? 'Active' : 'Unavailable'}
                              </span>
                            </div>
                            <h4 className="text-lg font-black">{item.name}</h4>
                            <p className="text-xs text-slate-400 leading-normal">
                              Flavors: {item.flavors.split('|').join(', ') || 'None'}
                            </p>
                            <div className="flex flex-col gap-1 mt-2">
                              {variantsList.map((v, idx) => (
                                <div key={idx} className="flex justify-between text-xs text-slate-400">
                                  <span>{v.name}</span>
                                  <span className="font-bold text-slate-300">{formatPrice(v.basePrice)}</span>
                                </div>
                              ))}
                            </div>
                          </div>

                          <div className="flex gap-2 border-t border-slate-800/80 pt-4 mt-2">
                            <button
                              onClick={() => handleEditItemClick(item)}
                              className="flex-1 flex items-center justify-center gap-1.5 py-2 bg-slate-800 hover:bg-slate-700/80 rounded-xl text-xs font-bold transition-colors"
                            >
                              <Edit2 className="w-3.5 h-3.5" />
                              Edit Item
                            </button>
                            <button
                              onClick={() => handleDeleteItem(item.id)}
                              className="px-3 py-2 bg-red-500/10 hover:bg-red-500/20 rounded-xl text-xs font-bold text-red-400 transition-colors border border-red-500/20"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* TAB 4: INVENTORY */}
            {activeTab === 'inventory' && (
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col gap-6">
                <div>
                  <h3 className="text-base font-bold">Aggregated Stock Level Audit</h3>
                  <p className="text-xs text-slate-400">Stock updates when orders are placed and served</p>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse text-sm">
                    <thead>
                      <tr className="border-b border-slate-800 text-slate-400 font-medium">
                        <th className="py-3 px-4">Item Name</th>
                        <th className="py-3 px-4">Current Stock</th>
                        <th className="py-3 px-4">Reorder Level</th>
                        <th className="py-3 px-4">Status</th>
                        <th className="py-3 px-4 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {inventory.map(item => {
                        const isLow = item.current_stock <= item.reorder_threshold;
                        return (
                          <tr key={item.id} className="border-b border-slate-800/50 hover:bg-slate-800/20 transition-colors">
                            <td className="py-4 px-4 font-bold text-slate-200">{item.item_name}</td>
                            <td className="py-4 px-4 font-black">
                              {item.current_stock} <span className="text-xs text-slate-400 font-medium">{item.unit}</span>
                            </td>
                            <td className="py-4 px-4 text-slate-400">
                              {item.reorder_threshold} <span className="text-xs text-slate-500">{item.unit}</span>
                            </td>
                            <td className="py-4 px-4">
                              <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${
                                isLow
                                  ? 'bg-amber-500/10 border-amber-500/20 text-amber-400'
                                  : 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400'
                              }`}>
                                {isLow ? 'Low Stock alert' : 'Healthy'}
                              </span>
                            </td>
                            <td className="py-4 px-4 text-right">
                              <button
                                onClick={() => {
                                  setEditingInventory(item);
                                  setShowInventoryModal(true);
                                }}
                                className="px-3.5 py-1.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/20 rounded-xl text-xs font-bold transition-all"
                              >
                                Adjust Stock
                              </button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Category Modal */}
      {showCategoryModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-sm p-6 flex flex-col gap-4">
            <h3 className="text-base font-bold">Add Category</h3>
            <input
              type="text"
              placeholder="e.g. Milk Tea"
              value={newCategoryName}
              onChange={e => setNewCategoryName(e.target.value)}
              className="bg-slate-800 border border-slate-700 px-4 py-2.5 rounded-xl text-sm text-slate-200 outline-none focus:border-emerald-500"
            />
            <div className="flex gap-3 justify-end mt-2">
              <button
                onClick={() => setShowCategoryModal(false)}
                className="px-4 py-2 text-sm text-slate-400 hover:text-slate-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleAddCategory}
                className="px-4 py-2 bg-emerald-500 text-slate-950 rounded-xl text-sm font-bold hover:bg-emerald-400 transition-colors"
              >
                Add Category
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Inventory Modal */}
      {showInventoryModal && editingInventory && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-sm p-6 flex flex-col gap-4">
            <div>
              <h3 className="text-base font-bold">Adjust Stock Level</h3>
              <p className="text-xs text-slate-400 mt-1">{editingInventory.item_name} &bull; Current: {editingInventory.current_stock} {editingInventory.unit}</p>
            </div>
            <input
              type="number"
              placeholder="e.g. 50 (or -10 to reduce)"
              value={stockAdjustment}
              onChange={e => setStockAdjustment(e.target.value)}
              className="bg-slate-800 border border-slate-700 px-4 py-2.5 rounded-xl text-sm text-slate-200 outline-none focus:border-emerald-500"
            />
            <div className="flex gap-3 justify-end mt-2">
              <button
                onClick={() => setShowInventoryModal(false)}
                className="px-4 py-2 text-sm text-slate-400 hover:text-slate-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveStock}
                className="px-4 py-2 bg-emerald-500 text-slate-950 rounded-xl text-sm font-bold hover:bg-emerald-400 transition-colors"
              >
                Apply Change
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Item Modal */}
      {showItemModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-6 flex flex-col gap-5 my-8">
            <h3 className="text-lg font-black">{editingItem ? 'Edit Product Item' : 'Add New Product Item'}</h3>

            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-400">Product Name</label>
                <input
                  type="text"
                  placeholder="e.g. Classic Pearl Milk Tea"
                  value={itemName}
                  onChange={e => setItemName(e.target.value)}
                  className="bg-slate-800 border border-slate-700 px-4 py-2 rounded-xl text-sm text-slate-200 outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-400">Category</label>
                <select
                  value={itemCategory}
                  onChange={e => setItemCategory(e.target.value)}
                  className="bg-slate-800 border border-slate-700 px-4 py-2 rounded-xl text-sm text-slate-200 outline-none focus:border-emerald-500"
                >
                  <option value="" disabled>Select a category</option>
                  {categories.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-400">Flavors (comma separated)</label>
                <input
                  type="text"
                  placeholder="e.g. Chocolate, Matcha, Wintermelon"
                  value={itemFlavors}
                  onChange={e => setItemFlavors(e.target.value)}
                  className="bg-slate-800 border border-slate-700 px-4 py-2 rounded-xl text-sm text-slate-200 outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex flex-col gap-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs font-semibold text-slate-400 font-bold">Sizes & Variant Prices</label>
                  <button
                    onClick={() => {
                      setItemVariants(prev => [
                        ...prev,
                        { id: 'size_' + Date.now().toString().slice(-4), name: '', basePrice: 0, priceByFlavor: {} }
                      ]);
                    }}
                    className="flex items-center gap-1 text-xs text-emerald-400 hover:text-emerald-300 font-bold"
                  >
                    <Plus className="w-3.5 h-3.5" /> Add Size
                  </button>
                </div>

                <div className="flex flex-col gap-2 max-h-40 overflow-y-auto pr-1">
                  {itemVariants.map((v, index) => (
                    <div key={index} className="flex gap-2 items-center">
                      <input
                        type="text"
                        placeholder="Size (e.g. Large / 16oz)"
                        value={v.name}
                        onChange={e => {
                          const updated = [...itemVariants];
                          updated[index].name = e.target.value;
                          updated[index].id = e.target.value.toLowerCase().replace(/[^a-z0-9]/g, '_');
                          setItemVariants(updated);
                        }}
                        className="flex-1 bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-xs outline-none focus:border-emerald-500"
                      />
                      <input
                        type="number"
                        placeholder="Price"
                        value={v.basePrice || ''}
                        onChange={e => {
                          const updated = [...itemVariants];
                          updated[index].basePrice = parseFloat(e.target.value) || 0;
                          setItemVariants(updated);
                        }}
                        className="w-24 bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-xs outline-none focus:border-emerald-500"
                      />
                      <button
                        onClick={() => setItemVariants(itemVariants.filter((_, idx) => idx !== index))}
                        className="p-1.5 hover:bg-slate-800 rounded-lg text-red-400 hover:text-red-300 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex gap-3 justify-end mt-4">
              <button
                onClick={() => {
                  setShowItemModal(false);
                  setEditingItem(null);
                }}
                className="px-4 py-2 text-sm text-slate-400 hover:text-slate-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveItem}
                className="px-5 py-2.5 bg-emerald-500 text-slate-950 rounded-xl text-sm font-bold hover:bg-emerald-400 transition-colors"
              >
                Save Product
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
