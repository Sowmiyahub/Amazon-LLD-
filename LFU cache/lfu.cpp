class Node{
public:
    int key;
    int value;
    int freq;
    Node* prev;
    Node* next;

    Node(int key, int value){
        this->key = key;
        this->value = value;
        freq=1;
        prev = nullptr;
        next = nullptr;
    }
};
class DLL{
public:
    Node* head;
    Node* tail;
    DLL(){
        head = new Node(0, 0);
        tail = new Node(0, 0);

        head->next = tail;
        tail->prev = head;
    }
    void removenode(Node* node){
        Node* prevnode = node->prev;
        Node* nextnode = node->next;

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
    Node* removeLRU(){
        if(head->next == tail) return nullptr;
        Node* node = tail->prev;
        removenode(node);
        return node;
    }
    bool empty(){
        return head->next == tail;
    }
};
class LFUCache {
    int minfreq;
    int capacity;
    unordered_map<int, Node*>keymap;
    unordered_map<int, DLL*>freqmap;
public:
    LFUCache(int capacity) {
        this->capacity = capacity;
        minfreq = 0;
    }
    
    int get(int key) {
        if(keymap.find(key) == keymap.end()) return -1;
        Node* node = keymap[key];
        updatefreq(node);
        return node->value;
    }
    
    void put(int key, int value) {
        //if capacity is 0
        if(capacity == 0) return;
        //if already that key is present in keymap
        if(keymap.find(key) != keymap.end()){
            Node* node = keymap[key];
            node->value = value;
            updatefreq(node);
            return;
        }
        //if the cache is full
        if(keymap.size() == capacity){
            DLL* list = freqmap[minfreq];
            Node* node = list->removeLRU();
            keymap.erase(node->key);
            delete node;
        }
        //when u have capacity
        Node* node = new Node(key, value);
        keymap[key] = node;
        minfreq = 1;
        if(freqmap.find(1) == freqmap.end()){
            freqmap[1] = new DLL();
        }
        freqmap[1] -> insertfront(node);
    }
private:
    void updatefreq(Node* node){
        int oldfreq = node->freq;
        DLL* oldlist = freqmap[oldfreq];
        oldlist->removenode(node);
        if(oldlist->empty()){
            if(oldfreq == minfreq){
                minfreq++;
            }
        }
        node->freq++;
        int newfreq = node->freq;
        if(freqmap.find(newfreq) == freqmap.end()){
            freqmap[newfreq] = new DLL();
        }
        freqmap[newfreq]->insertfront(node);
    }
};

/**
 * Your LFUCache object will be instantiated and called as such:
 * LFUCache* obj = new LFUCache(capacity);
 * int param_1 = obj->get(key);
 * obj->put(key,value);
 */