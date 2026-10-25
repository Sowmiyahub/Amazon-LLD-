class Node{
public:
    int key;
    int value;
    Node* next;
    Node* prev;
    Node(int key, int value){
        this->key = key;
        this->value = value;
        next = nullptr;
        prev = nullptr;
    }
};
class LRUCache {
    int capacity;
    Node* head;
    Node* tail;
    unordered_map<int, Node*>keymap;
public:
    LRUCache(int capacity) {
        this->capacity = capacity;
        head = new Node(0,0);
        tail = new Node(0,0);
        head->next = tail;
        tail->prev = head;
    }
    
    int get(int key) {
        if(keymap.find(key) == keymap.end()) return -1;
        Node* node = keymap[key];
        removenode(node);
        insertfront(node);
        return node->value;
    }
    
    void put(int key, int value) {
        if(keymap.find(key) != keymap.end()){
            Node* node = keymap[key];
            node->value = value;
            removenode(node);
            insertfront(node);
        }
        else{
            if(keymap.size() == capacity){
                Node* node = tail->prev;
                removenode(node);
                keymap.erase(node->key);
            }
            Node* node = new Node(key, value);
            keymap[key] = node;
            insertfront(node);
        }
    }
    void removenode(Node* node){
        Node* nextnode = node->next;
        Node* prevnode = node->prev;
        prevnode->next = nextnode;
        nextnode->prev = prevnode;
    }
    void insertfront(Node* node){
        Node* cur = head->next;
        head->next = node;
        node->prev = head;
        node->next = cur;
        cur->prev = node;
    }
};

/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache* obj = new LRUCache(capacity);
 * int param_1 = obj->get(key);
 * obj->put(key,value);
 */